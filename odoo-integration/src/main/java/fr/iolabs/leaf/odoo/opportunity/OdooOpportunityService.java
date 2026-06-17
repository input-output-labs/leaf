package fr.iolabs.leaf.odoo.opportunity;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OdooOpportunityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooOpportunityService.class);
	private static final String CRM_LEAD_MODEL = "crm.lead";
	private static final String CRM_TAG_MODEL = "crm.tag";
	private static final String PARTNER_MODEL = "res.partner";
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public int countOpportunitiesCreatedBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.countOpportunitiesCreatedBetween(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public int countOpportunitiesCreatedBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		Objects.requireNonNull(fromInclusive, "fromInclusive is required");
		Objects.requireNonNull(toInclusive, "toInclusive is required");
		if (fromInclusive.isAfter(toInclusive)) {
			throw new IllegalArgumentException("fromInclusive must be before or equal to toInclusive");
		}

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			List<Object> domain = this.resolveOpportunityDomainWithCreateDateBetween(fromInclusive, toInclusive);
			int count = this.odooRpcClient.searchCount(credentials, uid, CRM_LEAD_MODEL, domain);
			LOGGER.info(
				"Counted {} Odoo opportunities created between {} and {} (db={})",
				count,
				fromInclusive,
				toInclusive,
				credentials.getDb()
			);
			return count;
		} catch (OdooIntegrationException exception) {
			LOGGER.error(
				"Failed to count Odoo opportunities created between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error(
				"Failed to count Odoo opportunities created between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw new OdooIntegrationException(
				"Failed to count Odoo opportunities: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooOpportunity> listOpportunities(Integer limit) {
		return this.listOpportunities(this.odooCredentialsResolver.resolveFromApplicationConfig(), limit);
	}

	public List<OdooOpportunity> listOpportunities(String organizationId, Integer limit) {
		return this.listOpportunities(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), limit);
	}

	public List<OdooOpportunity> listOpportunities(OdooCredentials credentials, Integer limit) {
		Integer maxTotal = this.sanitizeMaxTotal(limit);
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			List<Object> domain = this.resolveOpportunityDomain();
			List<String> fields = this.resolveOpportunityFields(credentials, uid);
			List<Map<String, Object>> rows = this.fetchAllOpportunityRows(credentials, uid, domain, fields, maxTotal);
			return this.mapOpportunityRows(credentials, uid, rows);
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo opportunities", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo opportunities", exception);
			throw new OdooIntegrationException(
				"Failed to list Odoo opportunities: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooOpportunity> listOpportunitiesPage(OdooCredentials credentials, Integer limit, int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0");
		}
		int safeOffset = Math.min(offset, MAX_TOTAL_RECORDS);
		int pageLimit = this.sanitizePageLimit(limit);
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			List<Object> domain = this.resolveOpportunityDomain();
			List<String> fields = this.resolveOpportunityFields(credentials, uid);
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						CRM_LEAD_MODEL,
						domain,
						fields,
						pageLimit,
						safeOffset,
						"id desc"
					);
			return this.mapOpportunityRows(credentials, uid, rows);
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo opportunities page (offset={})", offset, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo opportunities page (offset={})", offset, exception);
			throw new OdooIntegrationException(
				"Failed to list Odoo opportunities: " + exception.getMessage(),
				exception
			);
		}
	}

	public OdooOpportunity getOpportunityById(int opportunityId) {
		return this.getOpportunityById(this.odooCredentialsResolver.resolveFromApplicationConfig(), opportunityId);
	}

	public OdooOpportunity getOpportunityById(OdooCredentials credentials, int opportunityId) {
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			List<String> fields = this.resolveOpportunityFields(credentials, uid);
			List<Map<String, Object>> rows =
				this.odooRpcClient.read(
						credentials,
						uid,
						CRM_LEAD_MODEL,
						List.of(opportunityId),
						fields
					);
			if (rows.isEmpty()) {
				return null;
			}
			List<OdooOpportunity> opportunities = this.mapOpportunityRows(credentials, uid, rows);
			return opportunities.isEmpty() ? null : opportunities.get(0);
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to fetch Odoo opportunity {}", opportunityId, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to fetch Odoo opportunity {}", opportunityId, exception);
			throw new OdooIntegrationException(
				"Failed to fetch Odoo opportunity: " + exception.getMessage(),
				exception
			);
		}
	}

	public OdooOpportunityCreateResult createOpportunity(OdooOpportunityData data) {
		return this.createOpportunity(this.odooCredentialsResolver.resolveFromApplicationConfig(), data);
	}

	public OdooOpportunityCreateResult createOpportunity(String organizationId, OdooOpportunityData data) {
		return this.createOpportunity(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), data);
	}

	public OdooOpportunityCreateResult createOpportunity(OdooCredentials credentials, OdooOpportunityData data) {
		try {
			data.validate();
			int uid = this.odooRpcClient.authenticate(credentials);
			Map<String, Object> values = toOdooValues(data);
			int priorityStars = clampPriorityStars(data.getPriorityStars());
			int opportunityId = this.odooRpcClient.createRecord(credentials, uid, CRM_LEAD_MODEL, values);
			if (priorityStars > 0) {
				this.odooRpcClient.writeRecord(
						credentials,
						uid,
						CRM_LEAD_MODEL,
						opportunityId,
						Map.of("priority", odooPriorityValue(priorityStars))
					);
			}
			LOGGER.info(
				"Odoo opportunity created: id={}, name={}, priority={}, db={}",
				opportunityId,
				data.getName(),
				priorityStars,
				credentials.getDb()
			);
			return OdooOpportunityCreateResult.success(opportunityId);
		} catch (OdooIntegrationException exception) {
			LOGGER.warn("Odoo opportunity creation failed: {}", exception.getMessage());
			return OdooOpportunityCreateResult.failure(exception.getMessage());
		} catch (RuntimeException exception) {
			LOGGER.error("Unexpected error while creating Odoo opportunity", exception);
			return OdooOpportunityCreateResult.failure(exception.getMessage());
		}
	}

	private Map<String, Object> toOdooValues(OdooOpportunityData data) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("name", data.getName().trim());
		values.put("type", "opportunity");
		OdooValueMapper.putIfHasText(values, "contact_name", data.getContactName());
		OdooValueMapper.putIfHasText(values, "email_from", data.getEmail());
		OdooValueMapper.putIfHasText(values, "phone", data.getPhone());
		OdooValueMapper.putIfHasText(values, "description", data.getDescription());
		OdooValueMapper.putIfHasText(values, "partner_name", data.getPartnerName());
		OdooValueMapper.putIfHasText(values, "street", data.getStreet());
		OdooValueMapper.putIfHasText(values, "city", data.getCity());
		OdooValueMapper.putIfHasText(values, "zip", data.getZip());
		if (data.getExpectedRevenue() != null) {
			values.put("expected_revenue", data.getExpectedRevenue());
		}
		int priorityStars = clampPriorityStars(data.getPriorityStars());
		if (priorityStars > 0) {
			values.put("priority", odooPriorityValue(priorityStars));
		}
		return values;
	}

	private int clampPriorityStars(int priorityStars) {
		return Math.max(0, Math.min(3, priorityStars));
	}

	private String odooPriorityValue(int priorityStars) {
		return String.valueOf(clampPriorityStars(priorityStars));
	}

	private List<Object> resolveOpportunityDomain() {
		return List.of(List.of("type", "=", "opportunity"), List.of("active", "=", true));
	}

	private List<Object> resolveOpportunityDomainWithCreateDateBetween(
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		List<Object> domain = new ArrayList<>(this.resolveOpportunityDomain());
		domain.add(List.of("create_date", ">=", this.toOdooDateTime(fromInclusive)));
		domain.add(List.of("create_date", "<=", this.toOdooDateTime(toInclusive)));
		return domain;
	}

	private String toOdooDateTime(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(ODOO_DATE_TIME);
	}

	private List<OdooOpportunity> mapOpportunityRows(
		OdooCredentials credentials,
		int uid,
		List<Map<String, Object>> rows
	) {
		Map<Integer, String> tagNamesById = this.resolveTagNames(credentials, uid, rows);
		Map<Integer, PartnerContactData> partnersById = this.resolvePartnerContacts(credentials, uid, rows);
		return mapOpportunities(rows, tagNamesById, partnersById);
	}

	private List<String> resolveOpportunityFields(OdooCredentials credentials, int uid) {
		Set<String> availableFields = OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, CRM_LEAD_MODEL));
		List<String> fields = new ArrayList<>(Arrays.asList("id", "name"));
		for (String candidate :
			Arrays.asList(
				"contact_name",
				"email_from",
				"phone",
				"expected_revenue",
				"partner_name",
				"partner_id",
				"stage_id",
				"tag_ids",
				"create_date"
			)) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private Map<Integer, String> resolveTagNames(
		OdooCredentials credentials,
		int uid,
		List<Map<String, Object>> rows
	) {
		Set<Integer> tagIds = new HashSet<>();
		for (Map<String, Object> row : rows) {
			tagIds.addAll(OdooValueMapper.asIntegerList(row.get("tag_ids")));
		}
		if (tagIds.isEmpty()) {
			return Map.of();
		}

		List<Map<String, Object>> tagRows =
			this.odooRpcClient.read(
					credentials,
					uid,
					CRM_TAG_MODEL,
					new ArrayList<>(tagIds),
					List.of("id", "name")
				);
		Map<Integer, String> tagNamesById = new HashMap<>();
		for (Map<String, Object> tagRow : tagRows) {
			Integer id = OdooValueMapper.asInteger(tagRow.get("id"));
			String name = OdooValueMapper.asString(tagRow.get("name"));
			if (id != null && name != null) {
				tagNamesById.put(id, name);
			}
		}
		return tagNamesById;
	}

	private Map<Integer, PartnerContactData> resolvePartnerContacts(
		OdooCredentials credentials,
		int uid,
		List<Map<String, Object>> rows
	) {
		Set<Integer> partnerIds = new HashSet<>();
		for (Map<String, Object> row : rows) {
			Integer partnerId = OdooValueMapper.asMany2OneId(row.get("partner_id"));
			if (partnerId != null) {
				partnerIds.add(partnerId);
			}
		}
		if (partnerIds.isEmpty()) {
			return Map.of();
		}

		Set<String> availableFields =
			OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PARTNER_MODEL));
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		for (String candidate : Arrays.asList("email", "phone", "mobile")) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}

		List<Map<String, Object>> partnerRows =
			this.odooRpcClient.read(
					credentials,
					uid,
					PARTNER_MODEL,
					new ArrayList<>(partnerIds),
					fields
				);
		Map<Integer, PartnerContactData> partnersById = new HashMap<>();
		for (Map<String, Object> partnerRow : partnerRows) {
			Integer id = OdooValueMapper.asInteger(partnerRow.get("id"));
			if (id == null) {
				continue;
			}
			partnersById.put(
				id,
				new PartnerContactData(
					OdooValueMapper.asString(partnerRow.get("name")),
					OdooValueMapper.asString(partnerRow.get("email")),
					this.coalesceText(
							OdooValueMapper.asString(partnerRow.get("phone")),
							OdooValueMapper.asString(partnerRow.get("mobile"))
						)
				)
			);
		}
		return partnersById;
	}

	private List<String> mapTagNames(Object rawTagIds, Map<Integer, String> tagNamesById) {
		List<String> tags = new ArrayList<>();
		for (Integer tagId : OdooValueMapper.asIntegerList(rawTagIds)) {
			String tagName = tagNamesById.get(tagId);
			if (tagName != null) {
				tags.add(tagName);
			}
		}
		return tags;
	}

	private List<OdooOpportunity> mapOpportunities(
		List<Map<String, Object>> rows,
		Map<Integer, String> tagNamesById,
		Map<Integer, PartnerContactData> partnersById
	) {
		List<OdooOpportunity> opportunities = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			Object expectedRevenue = row.get("expected_revenue");
			Double revenue = expectedRevenue instanceof Number number ? number.doubleValue() : null;
			Integer partnerId = OdooValueMapper.asMany2OneId(row.get("partner_id"));
			PartnerContactData partner = partnerId != null ? partnersById.get(partnerId) : null;
			OdooOpportunity opportunity =
				new OdooOpportunity(
					OdooValueMapper.asInteger(row.get("id")),
					OdooValueMapper.asString(row.get("name")),
					this.coalesceText(
							OdooValueMapper.asString(row.get("contact_name")),
							partner != null ? partner.name() : null
						),
					this.coalesceText(
							OdooValueMapper.asString(row.get("email_from")),
							partner != null ? partner.email() : null
						),
					this.coalesceText(
							OdooValueMapper.asString(row.get("phone")),
							partner != null ? partner.phone() : null
						),
					revenue,
					OdooValueMapper.asString(row.get("partner_name"))
				);
			opportunity.setStageId(OdooValueMapper.asMany2OneId(row.get("stage_id")));
			opportunity.setStageName(OdooValueMapper.asMany2OneDisplayName(row.get("stage_id")));
			opportunity.setTags(this.mapTagNames(row.get("tag_ids"), tagNamesById));
			opportunity.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
			opportunities.add(opportunity);
		}
		return opportunities;
	}

	private String coalesceText(String... values) {
		for (String value : values) {
			if (StringUtils.hasText(value)) {
				return value.trim();
			}
		}
		return null;
	}

	private record PartnerContactData(String name, String email, String phone) {}

	private List<Map<String, Object>> fetchAllOpportunityRows(
		OdooCredentials credentials,
		int uid,
		List<Object> domain,
		List<String> fields,
		Integer maxTotal
	) {
		List<Map<String, Object>> allRows = new ArrayList<>();
		int offset = 0;
		int pageNumber = 0;

		while (true) {
			int remaining = maxTotal != null ? maxTotal - allRows.size() : PAGE_SIZE;
			if (maxTotal != null && remaining <= 0) {
				break;
			}

			int pageLimit = maxTotal != null ? Math.min(PAGE_SIZE, remaining) : PAGE_SIZE;
			List<Map<String, Object>> page =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						CRM_LEAD_MODEL,
						domain,
						fields,
						pageLimit,
						offset,
						"id desc"
					);
			pageNumber++;
			allRows.addAll(page);
			LOGGER.debug(
				"Fetched Odoo opportunities page {} (offset={}, pageSize={}, totalSoFar={})",
				pageNumber,
				offset,
				page.size(),
				allRows.size()
			);

			if (page.size() < pageLimit) {
				break;
			}

			offset += page.size();
			if (offset >= MAX_TOTAL_RECORDS) {
				LOGGER.warn("Stopped Odoo opportunity pagination after {} records", offset);
				break;
			}
		}

		LOGGER.info("Fetched {} Odoo opportunities across {} page(s)", allRows.size(), pageNumber);
		return allRows;
	}

	private Integer sanitizeMaxTotal(Integer limit) {
		if (limit == null || limit <= 0) {
			return null;
		}
		return Math.min(limit, MAX_TOTAL_RECORDS);
	}

	private int sanitizePageLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return PAGE_SIZE;
		}
		return Math.min(limit, PAGE_SIZE);
	}
}
