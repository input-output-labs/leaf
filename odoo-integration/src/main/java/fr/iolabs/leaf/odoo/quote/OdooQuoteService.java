package fr.iolabs.leaf.odoo.quote;

import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.invoice.OdooInvoice;
import fr.iolabs.leaf.odoo.invoice.OdooInvoiceQueryService;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class OdooQuoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooQuoteService.class);
	private static final String SALE_ORDER_MODEL = "sale.order";
	private static final String SALE_ORDER_LINE_MODEL = "sale.order.line";
	private static final String SALE_INVOICE_WIZARD_MODEL = "sale.advance.payment.inv";
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final List<String> SIGNATURE_DATE_FIELD_CANDIDATES = List.of(
		"signature_date",
		"signed_on",
		"signed_date",
		"date_signature",
		"x_date_signature",
		"x_studio_date_signature"
	);

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	@Autowired
	private OdooInvoiceQueryService odooInvoiceQueryService;

	public List<OdooQuote> listQuotesSignedBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.listQuotesSignedBetween(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooQuote> listQuotesSignedBetween(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.listQuotesSignedBetween(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooQuote> listQuotesSignedBetween(
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
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_MODEL));
			String signatureDateField = this.resolveSignatureDateField(availableFields);
			List<Object> domain = this.resolveSignedQuoteDomain(signatureDateField, fromInclusive, toInclusive);
			List<String> fields = this.resolveQuoteFields(availableFields, signatureDateField);
			List<Map<String, Object>> rows = this.fetchAllQuoteRows(credentials, uid, domain, fields, null);
			List<OdooQuote> quotes = this.mapQuotes(credentials, rows, signatureDateField);
			LOGGER.info(
				"Listed {} Odoo quotes signed between {} and {} using field {} (db={})",
				quotes.size(),
				fromInclusive,
				toInclusive,
				signatureDateField,
				credentials.getDb()
			);
			return quotes;
		} catch (OdooIntegrationException exception) {
			LOGGER.error(
				"Failed to list Odoo quotes signed between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error(
				"Failed to list Odoo quotes signed between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw new OdooIntegrationException(
				"Failed to list Odoo quotes: " + exception.getMessage(),
				exception
			);
		}
	}

	private String resolveSignatureDateField(Set<String> availableFields) {
		for (String candidate : SIGNATURE_DATE_FIELD_CANDIDATES) {
			if (availableFields.contains(candidate)) {
				return candidate;
			}
		}
		throw new OdooIntegrationException(
			"No signature date field found on sale.order. Expected one of: " + SIGNATURE_DATE_FIELD_CANDIDATES
		);
	}

	private List<Object> resolveSignedQuoteDomain(
		String signatureDateField,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		List<Object> domain = new ArrayList<>();
		domain.add(List.of(signatureDateField, "!=", false));
		domain.add(List.of(signatureDateField, ">=", this.toOdooDateTime(fromInclusive)));
		domain.add(List.of(signatureDateField, "<=", this.toOdooDateTime(toInclusive)));
		return domain;
	}

	private List<String> resolveQuoteFields(Set<String> availableFields, String signatureDateField) {
		List<String> fields = new ArrayList<>(List.of("id", "name", signatureDateField));
		for (String candidate :
			Arrays.asList(
				"state",
				"create_date",
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

	private List<OdooQuote> mapQuotes(
		OdooCredentials credentials,
		List<Map<String, Object>> rows,
		String signatureDateField
	) {
		List<OdooQuote> quotes = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			Integer id = OdooValueMapper.asInteger(row.get("id"));
			if (id == null) {
				continue;
			}

			OdooQuote quote = new OdooQuote();
			quote.setId(id);
			quote.setName(OdooValueMapper.asString(row.get("name")));
			quote.setOdooUrl(this.buildQuoteUrl(credentials, id));
			quote.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
			quote.setSignedAt(OdooValueMapper.asZonedDateTime(row.get(signatureDateField)));
			quote.setStatus(OdooValueMapper.asString(row.get("state")));
			quote.setStatusLabel(this.toStatusLabel(quote.getStatus()));
			quote.setPartnerId(OdooValueMapper.asMany2OneId(row.get("partner_id")));
			quote.setPartnerName(OdooValueMapper.asMany2OneDisplayName(row.get("partner_id")));
			quote.setAmountTotal(this.asDouble(row.get("amount_total")));
			quote.setAmountUntaxed(this.asDouble(row.get("amount_untaxed")));
			quote.setCurrencyCode(OdooValueMapper.asMany2OneDisplayName(row.get("currency_id")));
			quotes.add(quote);
		}
		return quotes;
	}

	private String buildQuoteUrl(OdooCredentials credentials, int quoteId) {
		return (
			OdooValueMapper.ensureNoTrailingSlash(credentials.getUrl()) +
			"/web#id=" +
			quoteId +
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

	private List<Map<String, Object>> fetchAllQuoteRows(
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
						SALE_ORDER_MODEL,
						domain,
						fields,
						pageLimit,
						offset,
						signatureDateFieldOrder(fields)
					);
			pageNumber++;
			allRows.addAll(page);
			LOGGER.debug(
				"Fetched Odoo quotes page {} (offset={}, pageSize={}, totalSoFar={})",
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
				LOGGER.warn("Stopped Odoo quote pagination after {} records", offset);
				break;
			}
		}

		return allRows;
	}

	private String signatureDateFieldOrder(List<String> fields) {
		for (String candidate : SIGNATURE_DATE_FIELD_CANDIDATES) {
			if (fields.contains(candidate)) {
				return candidate + " desc";
			}
		}
		return "id desc";
	}

	private String toOdooDateTime(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(ODOO_DATE_TIME);
	}

	public OdooInvoice createInvoiceFromQuote(OdooCredentials credentials, int quoteId) {
		if (quoteId <= 0) {
			throw new IllegalArgumentException("quoteId must be a valid Odoo sale.order id");
		}

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Map<String, Object> order = this.readSaleOrderForInvoicing(credentials, uid, quoteId);
			String state = OdooValueMapper.asString(order.get("state"));
			if ("cancel".equals(state)) {
				throw new OdooIntegrationException("The Odoo quote is cancelled and cannot be invoiced");
			}

			List<Integer> existingInvoiceIds = OdooValueMapper.asIntegerList(order.get("invoice_ids"));
			if (!existingInvoiceIds.isEmpty()) {
				return this.odooInvoiceQueryService.getInvoiceById(credentials, this.latestId(existingInvoiceIds));
			}

			if (state == null || "draft".equals(state) || "sent".equals(state)) {
				this.odooRpcClient.executeKw(
					credentials,
					uid,
					SALE_ORDER_MODEL,
					"action_confirm",
					List.of(List.of(quoteId)),
					Map.of()
				);
			}

			this.createInvoiceThroughWizard(credentials, uid, quoteId);

			Map<String, Object> refreshed = this.readSaleOrderForInvoicing(credentials, uid, quoteId);
			List<Integer> createdInvoiceIds = OdooValueMapper.asIntegerList(refreshed.get("invoice_ids"));
			if (createdInvoiceIds.isEmpty()) {
				throw new OdooIntegrationException("Odoo did not create an invoice for this quote");
			}
			return this.odooInvoiceQueryService.getInvoiceById(credentials, this.latestId(createdInvoiceIds));
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to create an Odoo invoice from quote {}", quoteId, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to create an Odoo invoice from quote {}", quoteId, exception);
			throw new OdooIntegrationException(
				"Failed to create an Odoo invoice from quote: " + exception.getMessage(),
				exception
			);
		}
	}

	private void createInvoiceThroughWizard(OdooCredentials credentials, int uid, int quoteId) {
		Map<String, Object> context = new HashMap<>();
		context.put("active_model", SALE_ORDER_MODEL);
		context.put("active_id", quoteId);
		context.put("active_ids", List.of(quoteId));

		Map<String, Object> values = new HashMap<>();
		values.put("advance_payment_method", "delivered");
		values.put("sale_order_ids", List.of(List.of(6, 0, List.of(quoteId))));

		Object created = this.odooRpcClient.executeKw(
			credentials,
			uid,
			SALE_INVOICE_WIZARD_MODEL,
			"create",
			List.of(values),
			Map.of("context", context)
		);
		Integer wizardId = OdooValueMapper.extractCreatedId(created);
		if (wizardId == null || wizardId <= 0) {
			throw new OdooIntegrationException("Odoo did not open the invoice wizard for this quote");
		}

		this.odooRpcClient.executeKw(
			credentials,
			uid,
			SALE_INVOICE_WIZARD_MODEL,
			"create_invoices",
			List.of(List.of(wizardId)),
			Map.of("context", context)
		);
	}

	private Map<String, Object> readSaleOrderForInvoicing(OdooCredentials credentials, int uid, int quoteId) {
		List<Map<String, Object>> rows =
			this.odooRpcClient.read(
				credentials,
				uid,
				SALE_ORDER_MODEL,
				List.of(quoteId),
				List.of("id", "state", "invoice_ids")
			);
		if (rows.isEmpty()) {
			throw new NotFoundException("Odoo quote not found: " + quoteId);
		}
		return rows.get(0);
	}

	private int latestId(List<Integer> ids) {
		int latest = ids.get(0);
		for (Integer id : ids) {
			if (id != null && id > latest) {
				latest = id;
			}
		}
		return latest;
	}

	public OdooQuote createQuote(OdooCreateQuoteRequest request) {
		return this.createQuote(this.odooCredentialsResolver.resolveForApi(), request);
	}

	public OdooQuote getQuoteById(int quoteId) {
		return this.getQuoteById(this.odooCredentialsResolver.resolveForApi(), quoteId);
	}

	public OdooQuote getQuoteByReference(OdooCredentials credentials, String reference) {
		String value = reference == null ? "" : reference.trim();
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException("Quote reference is required");
		}
		if (value.matches("\\d+")) {
			try {
				return this.getQuoteById(credentials, Integer.parseInt(value));
			} catch (NumberFormatException exception) {
				throw new NotFoundException("Odoo quote not found: " + value);
			}
		}
		return this.getQuoteById(credentials, this.findQuoteIdByName(credentials, value));
	}

	public OdooQuote getQuoteById(OdooCredentials credentials, int quoteId) {
		if (quoteId <= 0) {
			throw new IllegalArgumentException("quoteId must be a valid Odoo sale.order id");
		}

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_MODEL));
			OdooQuote quote = this.readCreatedQuote(credentials, uid, quoteId, availableFields, false);
			if (quote == null) {
				throw new NotFoundException("Odoo quote not found: " + quoteId);
			}
			quote.setLines(this.readQuoteLines(credentials, uid, quoteId));
			return quote;
		} catch (NotFoundException exception) {
			throw exception;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to fetch Odoo quote {}", quoteId, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to fetch Odoo quote {}", quoteId, exception);
			throw new OdooIntegrationException("Failed to fetch Odoo quote: " + exception.getMessage(), exception);
		}
	}

	private int findQuoteIdByName(OdooCredentials credentials, String name) {
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			List<Object> domain = List.of(List.of("name", "=", name));
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(credentials, uid, SALE_ORDER_MODEL, domain, List.of("id", "name"), 1, 0, "id desc");
			if (rows.isEmpty()) {
				throw new NotFoundException("Odoo quote not found: " + name);
			}
			Integer id = OdooValueMapper.asInteger(rows.get(0).get("id"));
			if (id == null || id <= 0) {
				throw new NotFoundException("Odoo quote not found: " + name);
			}
			return id;
		} catch (NotFoundException exception) {
			throw exception;
		} catch (OdooIntegrationException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new OdooIntegrationException("Failed to find Odoo quote " + name + ": " + exception.getMessage(), exception);
		}
	}

	public OdooQuote createQuote(OdooCredentials credentials, OdooCreateQuoteRequest request) {
		this.validateCreateRequest(request);

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_MODEL));

			List<Object> orderLinesPayload = new ArrayList<>();
			for (OdooCreateQuoteLineRequest line : request.getLines()) {
				Map<String, Object> lineValues = new HashMap<>();
				lineValues.put("product_id", line.getProductId());
				lineValues.put("product_uom_qty", line.getQuantity());
				if (line.getUnitPrice() != null) {
					lineValues.put("price_unit", line.getUnitPrice());
				}
				orderLinesPayload.add(List.of(0, 0, lineValues));
			}

			Map<String, Object> orderValues = new HashMap<>();
			orderValues.put("partner_id", request.getPartnerId());
			orderValues.put("order_line", orderLinesPayload);
			if (request.getOpportunityId() != null && availableFields.contains("opportunity_id")) {
				orderValues.put("opportunity_id", request.getOpportunityId());
			}

			LOGGER.info(
				"Creating Odoo quote (partnerId={}, opportunityId={}, lines={})",
				request.getPartnerId(),
				request.getOpportunityId(),
				orderLinesPayload.size()
			);

			int createdQuoteId = this.odooRpcClient.createRecord(credentials, uid, SALE_ORDER_MODEL, orderValues);
			OdooQuote created = this.readCreatedQuote(credentials, uid, createdQuoteId, availableFields, true);
			LOGGER.info(
				"Odoo quote created successfully (quoteId={}, name={}, url={})",
				created.getId(),
				created.getName(),
				created.getOdooUrl()
			);
			return created;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to create Odoo quote", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to create Odoo quote", exception);
			throw new OdooIntegrationException("Failed to create Odoo quote: " + exception.getMessage(), exception);
		}
	}

	private void validateCreateRequest(OdooCreateQuoteRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Quote creation payload is required");
		}
		if (request.getPartnerId() == null || request.getPartnerId() <= 0) {
			throw new IllegalArgumentException("partnerId must be a valid Odoo partner id");
		}
		if (request.getLines() == null || request.getLines().isEmpty()) {
			throw new IllegalArgumentException("At least one quote line is required");
		}
		for (OdooCreateQuoteLineRequest line : request.getLines()) {
			if (line == null || line.getProductId() == null || line.getProductId() <= 0) {
				throw new IllegalArgumentException("Each quote line must have a valid productId");
			}
			if (line.getQuantity() == null || line.getQuantity() <= 0) {
				throw new IllegalArgumentException("Each quote line must have a quantity > 0");
			}
		}
	}

	private OdooQuote readCreatedQuote(
		OdooCredentials credentials,
		int uid,
		int quoteId,
		Set<String> availableFields,
		boolean fallbackIfMissing
	) {
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		for (String candidate :
			Arrays.asList(
				"state",
				"create_date",
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

		List<Map<String, Object>> rows =
			this.odooRpcClient.read(credentials, uid, SALE_ORDER_MODEL, List.of(quoteId), fields);
		if (rows.isEmpty()) {
			if (!fallbackIfMissing) {
				return null;
			}
			OdooQuote fallback = new OdooQuote();
			fallback.setId(quoteId);
			fallback.setName("SO/" + quoteId);
			fallback.setOdooUrl(this.buildQuoteUrl(credentials, quoteId));
			fallback.setStatus("draft");
			fallback.setStatusLabel(this.toStatusLabel("draft"));
			return fallback;
		}

		Map<String, Object> row = rows.get(0);
		OdooQuote quote = new OdooQuote();
		quote.setId(quoteId);
		quote.setName(OdooValueMapper.asString(row.get("name")));
		quote.setOdooUrl(this.buildQuoteUrl(credentials, quoteId));
		quote.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
		quote.setStatus(OdooValueMapper.asString(row.get("state")));
		quote.setStatusLabel(this.toStatusLabel(quote.getStatus()));
		quote.setPartnerId(OdooValueMapper.asMany2OneId(row.get("partner_id")));
		quote.setPartnerName(OdooValueMapper.asMany2OneDisplayName(row.get("partner_id")));
		quote.setAmountTotal(this.asDouble(row.get("amount_total")));
		quote.setAmountUntaxed(this.asDouble(row.get("amount_untaxed")));
		quote.setCurrencyCode(OdooValueMapper.asMany2OneDisplayName(row.get("currency_id")));
		return quote;
	}

	private List<OdooQuoteLine> readQuoteLines(OdooCredentials credentials, int uid, int quoteId) {
		Set<String> availableFields =
			OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, SALE_ORDER_LINE_MODEL));
		List<String> fields = new ArrayList<>(List.of("id"));
		for (String candidate :
			Arrays.asList(
				"product_id",
				"name",
				"product_uom_qty",
				"price_unit",
				"display_type"
			)) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}

		List<Object> domain = List.of(List.of("order_id", "=", quoteId));
		List<Map<String, Object>> rows =
			this.odooRpcClient.searchRead(
				credentials,
				uid,
				SALE_ORDER_LINE_MODEL,
				domain,
				fields,
				500,
				"id asc"
			);

		List<OdooQuoteLine> lines = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			String displayType = OdooValueMapper.asString(row.get("display_type"));
			if (StringUtils.hasText(displayType)) {
				continue;
			}

			Integer productId = OdooValueMapper.asMany2OneId(row.get("product_id"));
			if (productId == null) {
				productId = OdooValueMapper.asInteger(row.get("product_id"));
			}
			if (productId == null) {
				continue;
			}

			OdooQuoteLine line = new OdooQuoteLine();
			line.setId(OdooValueMapper.asInteger(row.get("id")));
			line.setProductId(productId);
			String productName = OdooValueMapper.asMany2OneDisplayName(row.get("product_id"));
			if (!StringUtils.hasText(productName)) {
				productName = OdooValueMapper.asString(row.get("name"));
			}
			line.setProductName(productName);
			line.setQuantity(this.asDouble(row.get("product_uom_qty")));
			line.setUnitPrice(this.asDouble(row.get("price_unit")));
			lines.add(line);
		}
		return lines;
	}
}
