package fr.iolabs.leaf.odoo.sales;

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
public class OdooSaleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooSaleService.class);
	private static final String SALE_ORDER_MODEL = "sale.order";
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public List<OdooSale> search(SaleSearchCriteria criteria) {
		return this.search(this.odooCredentialsResolver.resolveFromApplicationConfig(), criteria);
	}

	public List<OdooSale> search(String organizationId, SaleSearchCriteria criteria) {
		return this.search(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), criteria);
	}

	public List<OdooSale> search(OdooCredentials credentials, SaleSearchCriteria criteria) {
		Objects.requireNonNull(criteria, "criteria is required");
		this.validateCreatedPeriod(criteria.getCreatedAfter(), criteria.getCreatedBefore());

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_MODEL));
			List<Object> domain = this.resolveSearchDomain(criteria);
			List<String> fields = this.resolveSaleFields(availableFields);
			List<Map<String, Object>> rows =
				this.fetchAllSaleRows(credentials, uid, domain, fields, criteria.getLimit(), "create_date desc");
			List<OdooSale> sales = this.mapSales(credentials, rows);
			LOGGER.info(
				"Listed {} Odoo sales matching criteria (db={}, createdAfter={}, createdBefore={}, states={})",
				sales.size(),
				credentials.getDb(),
				criteria.getCreatedAfter(),
				criteria.getCreatedBefore(),
				criteria.getStates()
			);
			return sales;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to search Odoo sales", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to search Odoo sales", exception);
			throw new OdooIntegrationException("Failed to search Odoo sales: " + exception.getMessage(), exception);
		}
	}

	public OdooSale getById(int saleId) {
		return this.getById(this.odooCredentialsResolver.resolveFromApplicationConfig(), saleId);
	}

	public OdooSale getById(String organizationId, int saleId) {
		return this.getById(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), saleId);
	}

	public OdooSale getById(OdooCredentials credentials, int saleId) {
		if (saleId <= 0) {
			return null;
		}

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_MODEL));
			List<String> fields = this.resolveSaleFields(availableFields);
			List<Map<String, Object>> rows =
				this.odooRpcClient.read(credentials, uid, SALE_ORDER_MODEL, List.of(saleId), fields);
			if (rows.isEmpty()) {
				return null;
			}
			return this.mapSale(credentials, rows.get(0));
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to get Odoo sale {}", saleId, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to get Odoo sale {}", saleId, exception);
			throw new OdooIntegrationException("Failed to get Odoo sale: " + exception.getMessage(), exception);
		}
	}

	private void validateCreatedPeriod(ZonedDateTime createdAfter, ZonedDateTime createdBefore) {
		if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
			throw new IllegalArgumentException("createdAfter must be before or equal to createdBefore");
		}
	}

	private List<Object> resolveSearchDomain(SaleSearchCriteria criteria) {
		List<Object> domain = new ArrayList<>();
		if (criteria.getCreatedAfter() != null) {
			domain.add(List.of("create_date", ">=", this.toOdooDateTime(criteria.getCreatedAfter())));
		}
		if (criteria.getCreatedBefore() != null) {
			domain.add(List.of("create_date", "<=", this.toOdooDateTime(criteria.getCreatedBefore())));
		}
		if (criteria.getStates() != null && !criteria.getStates().isEmpty()) {
			domain.add(List.of("state", "in", criteria.getStates()));
		}
		return domain;
	}

	private List<String> resolveSaleFields(Set<String> availableFields) {
		List<String> fields = new ArrayList<>(List.of("id", "name", "create_date"));
		for (String candidate :
			Arrays.asList(
				"state",
				"date_order",
				"partner_id",
				"amount_total",
				"amount_untaxed",
				"currency_id"
			)) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private List<OdooSale> mapSales(OdooCredentials credentials, List<Map<String, Object>> rows) {
		List<OdooSale> sales = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			OdooSale sale = this.mapSale(credentials, row);
			if (sale != null) {
				sales.add(sale);
			}
		}
		return sales;
	}

	private OdooSale mapSale(OdooCredentials credentials, Map<String, Object> row) {
		Integer id = OdooValueMapper.asInteger(row.get("id"));
		if (id == null) {
			return null;
		}

		OdooSale sale = new OdooSale();
		sale.setId(id);
		sale.setName(OdooValueMapper.asString(row.get("name")));
		sale.setOdooUrl(this.buildSaleUrl(credentials, id));
		sale.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
		sale.setDateOrder(OdooValueMapper.asZonedDateTime(row.get("date_order")));
		sale.setStatus(OdooValueMapper.asString(row.get("state")));
		sale.setStatusLabel(this.toStatusLabel(sale.getStatus()));
		sale.setPartnerId(OdooValueMapper.asMany2OneId(row.get("partner_id")));
		sale.setPartnerName(OdooValueMapper.asMany2OneDisplayName(row.get("partner_id")));
		sale.setAmountTotal(this.asDouble(row.get("amount_total")));
		sale.setAmountUntaxed(this.asDouble(row.get("amount_untaxed")));
		sale.setCurrencyCode(OdooValueMapper.asMany2OneDisplayName(row.get("currency_id")));
		return sale;
	}

	private String buildSaleUrl(OdooCredentials credentials, int saleId) {
		return (
			OdooValueMapper.ensureNoTrailingSlash(credentials.getUrl()) +
			"/web#id=" +
			saleId +
			"&model=" +
			SALE_ORDER_MODEL +
			"&view_type=form"
		);
	}

	private String toStatusLabel(String status) {
		if (!StringUtils.hasText(status)) {
			return null;
		}
		return switch (status) {
			case "draft" -> "Brouillon";
			case "sent" -> "Envoyé";
			case "sale" -> "Confirmé";
			case "done" -> "Verrouillé";
			case "cancel" -> "Annulé";
			default -> status;
		};
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}

	private List<Map<String, Object>> fetchAllSaleRows(
		OdooCredentials credentials,
		int uid,
		List<Object> domain,
		List<String> fields,
		Integer maxTotal,
		String order
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
						SALE_ORDER_MODEL,
						domain,
						fields,
						pageLimit,
						offset,
						order
					);
			pageNumber++;
			allRows.addAll(page);
			LOGGER.debug(
				"Fetched Odoo sales page {} (offset={}, pageSize={}, totalSoFar={})",
				pageNumber,
				offset,
				page.size(),
				allRows.size()
			);

			if (page.size() < pageLimit) {
				break;
			}

			offset += page.size();
			if (maxTotal == null && offset >= MAX_TOTAL_RECORDS) {
				LOGGER.warn("Stopped Odoo sales pagination after {} records", offset);
				break;
			}
		}

		return allRows;
	}

	private String toOdooDateTime(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(ODOO_DATE_TIME);
	}
}
