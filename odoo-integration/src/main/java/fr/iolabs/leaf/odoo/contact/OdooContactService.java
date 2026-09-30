package fr.iolabs.leaf.odoo.contact;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OdooContactService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooContactService.class);
	private static final String PARTNER_MODEL = "res.partner";
	private static final int DEFAULT_LIMIT = 50;
	private static final int MAX_LIMIT = 200;

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public List<OdooContact> listContacts(Integer limit) {
		return this.listContacts(this.odooCredentialsResolver.resolveFromCurrentOrganization(), limit);
	}

	public List<OdooContact> listContacts(String organizationId, Integer limit) {
		return this.listContacts(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), limit);
	}

	public List<OdooContact> listContacts(OdooCredentials credentials, Integer limit) {
		return this.listContacts(credentials, limit, null);
	}

	public List<OdooContact> listContacts(OdooCredentials credentials, Integer limit, String query) {
		int safeLimit = sanitizeLimit(limit);
		String normalizedQuery = this.normalizeQuery(query);
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			ContactFieldSelection fieldSelection = this.resolveContactFieldSelection(credentials, uid);
			List<Object> domain = this.resolveContactDomain(normalizedQuery, fieldSelection);
			String order = normalizedQuery == null ? "id desc" : "name asc";
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						PARTNER_MODEL,
						domain,
						fieldSelection.fields(),
						safeLimit,
						order
					);
			List<OdooContact> contacts = mapContacts(rows, fieldSelection.companyFieldName());
			LOGGER.debug("Listed {} Odoo contacts (query={})", contacts.size(), normalizedQuery != null);
			return contacts;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo contacts", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo contacts", exception);
			throw new OdooIntegrationException("Failed to list Odoo contacts: " + exception.getMessage(), exception);
		}
	}

	public OdooContact getContactById(String contactId) {
		return this.getContactById(this.odooCredentialsResolver.resolveFromCurrentOrganization(), contactId);
	}

	public OdooContact getContactById(String organizationId, String contactId) {
		return this.getContactById(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), contactId);
	}

	public OdooContact getContactById(OdooCredentials credentials, String contactId) {
		if (!StringUtils.hasText(contactId)) {
			return null;
		}
		Integer contactIdAsInt = OdooValueMapper.asInteger(contactId);
		if (contactIdAsInt == null) {
			return null;
		}
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			ContactFieldSelection fieldSelection = this.resolveContactFieldSelection(credentials, uid);
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						PARTNER_MODEL,
						List.of(List.of("id", "=", contactIdAsInt)),
						fieldSelection.fields(),
						1,
						null
					);
			List<OdooContact> contacts = mapContacts(rows, fieldSelection.companyFieldName());
			return contacts.isEmpty() ? null : contacts.get(0);
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to fetch Odoo contact by id={}", contactId, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to fetch Odoo contact by id={}", contactId, exception);
			throw new OdooIntegrationException(
				"Failed to fetch Odoo contact by id: " + exception.getMessage(),
				exception
			);
		}
	}

	private ContactFieldSelection resolveContactFieldSelection(OdooCredentials credentials, int uid) {
		Set<String> availableFields = OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PARTNER_MODEL));

		List<String> fields = new ArrayList<>(Arrays.asList("id", "name"));
		if (availableFields.contains("email")) {
			fields.add("email");
		}
		if (availableFields.contains("phone")) {
			fields.add("phone");
		}
		if (availableFields.contains("mobile")) {
			fields.add("mobile");
		}
		if (availableFields.contains("street")) {
			fields.add("street");
		}
		if (availableFields.contains("street2")) {
			fields.add("street2");
		}
		if (availableFields.contains("zip")) {
			fields.add("zip");
		}
		if (availableFields.contains("city")) {
			fields.add("city");
		}
		if (availableFields.contains("country_id")) {
			fields.add("country_id");
		}

		String companyFieldName = null;
		for (String candidate : Arrays.asList("company_name", "commercial_company_name", "parent_name")) {
			if (availableFields.contains(candidate)) {
				companyFieldName = candidate;
				fields.add(candidate);
				break;
			}
		}
		return new ContactFieldSelection(fields, companyFieldName);
	}

	private List<OdooContact> mapContacts(List<Map<String, Object>> rows, String companyFieldName) {
		List<OdooContact> contacts = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			contacts.add(
				new OdooContact(
					OdooValueMapper.asInteger(row.get("id")),
					OdooValueMapper.asString(row.get("name")),
					OdooValueMapper.asString(row.get("email")),
					OdooValueMapper.asString(row.get("phone")),
					OdooValueMapper.asString(row.get("mobile")),
					companyFieldName != null ? OdooValueMapper.asString(row.get(companyFieldName)) : null,
					OdooValueMapper.asString(row.get("street")),
					OdooValueMapper.asString(row.get("street2")),
					OdooValueMapper.asString(row.get("zip")),
					OdooValueMapper.asString(row.get("city")),
					OdooValueMapper.asMany2OneDisplayName(row.get("country_id"))
				)
			);
		}
		return contacts;
	}

	private List<Object> resolveContactDomain(String query, ContactFieldSelection fieldSelection) {
		List<Object> domain = new ArrayList<>();
		domain.add(List.of("active", "=", true));
		domain.add(List.of("type", "!=", "private"));
		if (query == null) {
			return domain;
		}

		List<List<Object>> matches = new ArrayList<>();
		for (String field : List.of("name", "email", "phone", "mobile")) {
			if (fieldSelection.fields().contains(field)) {
				matches.add(List.of(field, "ilike", query));
			}
		}
		if (fieldSelection.companyFieldName() != null) {
			matches.add(List.of(fieldSelection.companyFieldName(), "ilike", query));
		}
		Integer contactId = this.parseContactId(query);
		if (contactId != null) {
			matches.add(List.of("id", "=", contactId));
		}
		for (int i = 0; i < matches.size() - 1; i++) {
			domain.add("|");
		}
		domain.addAll(matches);
		return domain;
	}

	private String normalizeQuery(String query) {
		if (!StringUtils.hasText(query)) {
			return null;
		}
		String trimmed = query.trim();
		return trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
	}

	private Integer parseContactId(String query) {
		if (query.chars().anyMatch(character -> !Character.isDigit(character))) {
			return null;
		}
		try {
			int contactId = Integer.parseInt(query);
			return contactId > 0 ? contactId : null;
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private int sanitizeLimit(Integer limit) {
		int value = limit != null ? limit : DEFAULT_LIMIT;
		if (value <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(value, MAX_LIMIT);
	}

	private record ContactFieldSelection(List<String> fields, String companyFieldName) {}
}
