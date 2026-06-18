package fr.iolabs.leaf.odoo.invoice;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.accounting.OdooInvoicePaymentsWidgetParser;
import fr.iolabs.leaf.odoo.product.OdooProduct;
import fr.iolabs.leaf.odoo.product.OdooProductQueryService;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class OdooInvoiceQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooInvoiceQueryService.class);
	private static final String ACCOUNT_MOVE_MODEL = "account.move";
	private static final String ACCOUNT_MOVE_LINE_MODEL = "account.move.line";
	private static final int INVOICE_ID_CHUNK_SIZE = 100;
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter ODOO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	@Autowired
	private OdooProductQueryService odooProductQueryService;

	public List<OdooInvoice> listInvoicesPaidBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.listInvoicesPaidBetween(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoice> listInvoicesPaidBetween(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.listInvoicesPaidBetween(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoice> listInvoicesPaidBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		this.validatePeriod(fromInclusive, toInclusive);

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Map<String, Object> moveFields = this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_MOVE_MODEL);
			Set<String> availableMoveFields = OdooValueMapper.toFieldNames(moveFields);
			List<String> fields = this.resolveInvoiceFields(availableMoveFields, null, true);
			List<Object> domain = this.resolvePaidInvoiceDomain();
			List<Map<String, Object>> rows =
				this.fetchAllInvoiceRows(credentials, uid, domain, fields, "create_date desc");

			List<OdooInvoice> invoices = new ArrayList<>();
			for (Map<String, Object> row : rows) {
				OdooInvoice invoice = this.mapInvoice(credentials, row, null, true);
				if (invoice.getSignedAt() == null || !this.isWithinPeriod(invoice.getSignedAt(), fromInclusive, toInclusive)) {
					continue;
				}
				invoices.add(invoice);
			}

			LOGGER.info(
				"Listed {} Odoo invoices paid between {} and {} via invoice_payments_widget (db={})",
				invoices.size(),
				fromInclusive,
				toInclusive,
				credentials.getDb()
			);
			return invoices;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo invoices paid between {} and {}", fromInclusive, toInclusive, exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo invoices paid between {} and {}", fromInclusive, toInclusive, exception);
			throw new OdooIntegrationException(
				"Failed to list Odoo invoices paid: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.listPaidInvoiceLinesBetween(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.listPaidInvoiceLinesBetween(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		this.validatePeriod(fromInclusive, toInclusive);

		try {
			List<OdooInvoice> invoices = this.listInvoicesPaidBetween(credentials, fromInclusive, toInclusive);
			if (invoices.isEmpty()) {
				return List.of();
			}

			Map<Integer, OdooInvoice> invoicesById = new LinkedHashMap<>();
			List<Integer> invoiceIds = new ArrayList<>();
			for (OdooInvoice invoice : invoices) {
				if (invoice.getId() == null) {
					continue;
				}
				invoicesById.put(invoice.getId(), invoice);
				invoiceIds.add(invoice.getId());
			}
			if (invoiceIds.isEmpty()) {
				return List.of();
			}
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableLineFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_MOVE_LINE_MODEL));
			List<String> lineFields = this.resolveInvoiceLineFields(availableLineFields);
			List<Map<String, Object>> rows = this.fetchInvoiceLineRows(credentials, uid, invoiceIds, lineFields);
			Set<Integer> productIds = this.collectProductIds(rows);
			Map<Integer, OdooProduct> productsById = this.odooProductQueryService.findByIds(credentials, uid, productIds);
			List<OdooInvoiceLine> lines = this.mapInvoiceLines(rows, invoicesById, productsById);
			LOGGER.info(
				"Listed {} Odoo paid invoice lines between {} and {} (db={})",
				lines.size(),
				fromInclusive,
				toInclusive,
				credentials.getDb()
			);
			return lines;
		} catch (OdooIntegrationException exception) {
			LOGGER.error(
				"Failed to list Odoo paid invoice lines between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error(
				"Failed to list Odoo paid invoice lines between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw new OdooIntegrationException(
				"Failed to list Odoo paid invoice lines: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.listInvoicesUnpaidDuringPeriod(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.listInvoicesUnpaidDuringPeriod(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			fromInclusive,
			toInclusive
		);
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		this.validatePeriod(fromInclusive, toInclusive);

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Map<String, Object> moveFields = this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_MOVE_MODEL);
			Set<String> availableMoveFields = OdooValueMapper.toFieldNames(moveFields);
			List<String> fields = this.resolveInvoiceFields(availableMoveFields, null, true);
			List<Object> domain = this.resolveUnpaidDuringPeriodDomain(toInclusive);

			List<Map<String, Object>> rows = this.fetchAllInvoiceRows(credentials, uid, domain, fields, "create_date desc");
			List<OdooInvoice> invoices = new ArrayList<>();
			for (Map<String, Object> row : rows) {
				OdooInvoice invoice = this.mapInvoice(credentials, row, null, true);
				if (this.isUnpaidDuringPeriod(invoice, toInclusive)) {
					invoices.add(invoice);
				}
			}

			LOGGER.info(
				"Listed {} Odoo invoices unpaid during period {} to {} via invoice_payments_widget (db={})",
				invoices.size(),
				fromInclusive,
				toInclusive,
				credentials.getDb()
			);
			return invoices;
		} catch (OdooIntegrationException exception) {
			LOGGER.error(
				"Failed to list Odoo invoices unpaid during period {} to {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error(
				"Failed to list Odoo invoices unpaid during period {} to {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw new OdooIntegrationException(
				"Failed to list Odoo unpaid invoices: " + exception.getMessage(),
				exception
			);
		}
	}

	private void validatePeriod(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive is required");
		Objects.requireNonNull(toInclusive, "toInclusive is required");
		if (fromInclusive.isAfter(toInclusive)) {
			throw new IllegalArgumentException("fromInclusive must be before or equal to toInclusive");
		}
	}

	private List<Object> resolvePaidInvoiceDomain() {
		List<Object> domain = new ArrayList<>(this.basePostedCustomerInvoiceDomain());
		domain.add(List.of("payment_state", "=", "paid"));
		return domain;
	}

	private boolean isWithinPeriod(ZonedDateTime date, ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return !date.isBefore(fromInclusive) && !date.isAfter(toInclusive);
	}

	private boolean isUnpaidDuringPeriod(OdooInvoice invoice, ZonedDateTime periodEndInclusive) {
		String paymentStatus = invoice.getPaymentStatus();
		if ("not_paid".equals(paymentStatus) || "partial".equals(paymentStatus) || "in_payment".equals(paymentStatus)) {
			return true;
		}
		if ("paid".equals(paymentStatus)) {
			ZonedDateTime paymentDate = invoice.getSignedAt();
			return paymentDate != null && paymentDate.isAfter(periodEndInclusive);
		}
		return false;
	}

	private List<Object> resolveUnpaidDuringPeriodDomain(ZonedDateTime periodEndInclusive) {
		List<Object> domain = new ArrayList<>(this.basePostedCustomerInvoiceDomain());
		domain.add(
			List.of(
				"create_date",
				"<=",
				this.toOdooDateTime(periodEndInclusive)
			)
		);
		return domain;
	}

	private List<Object> basePostedCustomerInvoiceDomain() {
		return new ArrayList<>(
			List.of(
				List.of("move_type", "=", "out_invoice"),
				List.of("state", "=", "posted")
			)
		);
	}


	private List<String> resolveInvoiceFields(
		Set<String> availableFields,
		String paymentDateField,
		boolean includePaymentsWidget
	) {
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		if (paymentDateField != null) {
			fields.add(paymentDateField);
		}
		if (includePaymentsWidget && availableFields.contains("invoice_payments_widget")) {
			fields.add("invoice_payments_widget");
		}
		for (String candidate :
			Arrays.asList(
				"state",
				"payment_state",
				"create_date",
				"invoice_date",
				"invoice_date_due",
				"partner_id",
				"amount_total",
				"amount_untaxed",
				"amount_residual",
				"currency_id"
			)) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private List<String> resolveInvoiceLineFields(Set<String> availableFields) {
		List<String> fields = new ArrayList<>(List.of("id", "name", "move_id"));
		for (String candidate : Arrays.asList("price_subtotal", "display_type", "product_id", "quantity", "purchase_price")) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private Set<Integer> collectProductIds(List<Map<String, Object>> rows) {
		Set<Integer> productIds = new HashSet<>();
		for (Map<String, Object> row : rows) {
			Integer productId = OdooValueMapper.asMany2OneId(row.get("product_id"));
			if (productId != null) {
				productIds.add(productId);
			}
		}
		return productIds;
	}

	private List<OdooInvoiceLine> mapInvoiceLines(
		List<Map<String, Object>> rows,
		Map<Integer, OdooInvoice> invoicesById,
		Map<Integer, OdooProduct> productsById
	) {
		List<OdooInvoiceLine> lines = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			if (!this.isProductLine(row)) {
				continue;
			}

			Integer lineId = OdooValueMapper.asInteger(row.get("id"));
			Integer invoiceId = OdooValueMapper.asMany2OneId(row.get("move_id"));
			if (lineId == null || invoiceId == null) {
				continue;
			}

			OdooInvoice invoice = invoicesById.get(invoiceId);
			if (invoice == null) {
				continue;
			}

			Double amountUntaxed = this.asDouble(row.get("price_subtotal"));
			if (amountUntaxed == null || amountUntaxed == 0d) {
				continue;
			}

			Integer productId = OdooValueMapper.asMany2OneId(row.get("product_id"));
			Double quantity = this.asDouble(row.get("quantity"));
			if (quantity == null) {
				quantity = 0d;
			}
			Double unitCost = this.resolveUnitCost(row, productsById, productId);
			Double totalCost = quantity * unitCost;

			OdooInvoiceLine line = new OdooInvoiceLine();
			line.setId(lineId);
			line.setInvoiceId(invoiceId);
			line.setInvoiceName(invoice.getName());
			line.setLabel(OdooValueMapper.asString(row.get("name")));
			line.setAmountUntaxed(amountUntaxed);
			line.setProductId(productId);
			line.setQuantity(quantity);
			line.setUnitCost(unitCost);
			line.setTotalCost(totalCost);
			line.setPaidAt(invoice.getSignedAt());
			lines.add(line);
		}
		return lines;
	}

	private Double resolveUnitCost(Map<String, Object> row, Map<Integer, OdooProduct> productsById, Integer productId) {
		Double purchasePrice = this.asDouble(row.get("purchase_price"));
		if (purchasePrice != null && purchasePrice > 0d) {
			return purchasePrice;
		}
		if (productId == null) {
			return 0d;
		}
		OdooProduct product = productsById.get(productId);
		if (product == null || product.getStandardPrice() == null) {
			return 0d;
		}
		return product.getStandardPrice();
	}

	private boolean isProductLine(Map<String, Object> row) {
		String displayType = OdooValueMapper.asString(row.get("display_type"));
		return !"line_section".equals(displayType) && !"line_note".equals(displayType);
	}

	private List<Map<String, Object>> fetchInvoiceLineRows(
		OdooCredentials credentials,
		int uid,
		List<Integer> invoiceIds,
		List<String> fields
	) {
		List<Map<String, Object>> allRows = new ArrayList<>();
		for (int offset = 0; offset < invoiceIds.size(); offset += INVOICE_ID_CHUNK_SIZE) {
			List<Integer> chunk = invoiceIds.subList(offset, Math.min(offset + INVOICE_ID_CHUNK_SIZE, invoiceIds.size()));
			List<Object> domain = List.of(List.of("move_id", "in", chunk));
			allRows.addAll(this.fetchAllMoveLineRows(credentials, uid, domain, fields));
		}
		return allRows;
	}

	private List<Map<String, Object>> fetchAllMoveLineRows(
		OdooCredentials credentials,
		int uid,
		List<Object> domain,
		List<String> fields
	) {
		List<Map<String, Object>> allRows = new ArrayList<>();
		int offset = 0;
		while (true) {
			List<Map<String, Object>> page =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						ACCOUNT_MOVE_LINE_MODEL,
						domain,
						fields,
						PAGE_SIZE,
						offset,
						"id asc"
					);
			allRows.addAll(page);
			if (page.size() < PAGE_SIZE) {
				break;
			}
			offset += page.size();
			if (offset >= MAX_TOTAL_RECORDS) {
				LOGGER.warn("Stopped Odoo invoice line pagination after {} records", offset);
				break;
			}
		}
		return allRows;
	}

	private List<OdooInvoice> mapInvoices(
		OdooCredentials credentials,
		List<Map<String, Object>> rows,
		String paymentDateField,
		boolean usePaymentsWidget
	) {
		Map<Integer, OdooInvoice> uniqueById = new LinkedHashMap<>();
		for (Map<String, Object> row : rows) {
			OdooInvoice invoice = this.mapInvoice(credentials, row, paymentDateField, usePaymentsWidget);
			if (invoice.getId() != null) {
				uniqueById.put(invoice.getId(), invoice);
			}
		}
		return new ArrayList<>(uniqueById.values());
	}

	private OdooInvoice mapInvoice(
		OdooCredentials credentials,
		Map<String, Object> row,
		String paymentDateField,
		boolean usePaymentsWidget
	) {
		Integer id = OdooValueMapper.asInteger(row.get("id"));
		if (id == null) {
			return new OdooInvoice();
		}

		OdooInvoice invoice = new OdooInvoice();
		invoice.setId(id);
		invoice.setName(OdooValueMapper.asString(row.get("name")));
		invoice.setOdooUrl(this.buildInvoiceUrl(credentials, id));
		invoice.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
		invoice.setInvoiceDate(this.asLocalDate(row.get("invoice_date")));
		invoice.setDueDate(this.asLocalDate(row.get("invoice_date_due")));
		invoice.setSignedAt(this.resolvePaymentDate(row, paymentDateField, usePaymentsWidget));
		invoice.setStatus(OdooValueMapper.asString(row.get("state")));
		invoice.setStatusLabel(this.toStatusLabel(invoice.getStatus()));
		invoice.setPaymentStatus(OdooValueMapper.asString(row.get("payment_state")));
		invoice.setPaymentStatusLabel(this.toPaymentStatusLabel(invoice.getPaymentStatus()));
		invoice.setPartnerId(OdooValueMapper.asMany2OneId(row.get("partner_id")));
		invoice.setPartnerName(OdooValueMapper.asMany2OneDisplayName(row.get("partner_id")));
		invoice.setAmountTotal(this.asDouble(row.get("amount_total")));
		invoice.setAmountUntaxed(this.asDouble(row.get("amount_untaxed")));
		invoice.setAmountResidual(this.asDouble(row.get("amount_residual")));
		invoice.setCurrencyCode(OdooValueMapper.asMany2OneDisplayName(row.get("currency_id")));
		return invoice;
	}

	private ZonedDateTime resolvePaymentDate(
		Map<String, Object> row,
		String paymentDateField,
		boolean usePaymentsWidget
	) {
		if (usePaymentsWidget && "paid".equals(OdooValueMapper.asString(row.get("payment_state")))) {
			ZonedDateTime paymentDate =
				OdooInvoicePaymentsWidgetParser.resolveLatestPaymentDate(row.get("invoice_payments_widget"));
			if (paymentDate != null) {
				return paymentDate;
			}
		}
		if (paymentDateField != null) {
			return OdooValueMapper.asZonedDateTime(row.get(paymentDateField));
		}
		return null;
	}

	private List<Map<String, Object>> fetchAllInvoiceRows(
		OdooCredentials credentials,
		int uid,
		List<Object> domain,
		List<String> fields,
		String order
	) {
		List<Map<String, Object>> allRows = new ArrayList<>();
		int offset = 0;
		int pageNumber = 0;

		while (true) {
			List<Map<String, Object>> page =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						ACCOUNT_MOVE_MODEL,
						domain,
						fields,
						PAGE_SIZE,
						offset,
						order
					);
			pageNumber++;
			allRows.addAll(page);
			LOGGER.debug(
				"Fetched Odoo invoices page {} (offset={}, pageSize={}, totalSoFar={})",
				pageNumber,
				offset,
				page.size(),
				allRows.size()
			);

			if (page.size() < PAGE_SIZE) {
				break;
			}

			offset += page.size();
			if (offset >= MAX_TOTAL_RECORDS) {
				LOGGER.warn("Stopped Odoo invoice pagination after {} records", offset);
				break;
			}
		}

		return allRows;
	}

	private String buildInvoiceUrl(OdooCredentials credentials, int invoiceId) {
		return (
			OdooValueMapper.ensureNoTrailingSlash(credentials.getUrl()) +
			"/web#id=" +
			invoiceId +
			"&model=" +
			ACCOUNT_MOVE_MODEL +
			"&view_type=form"
		);
	}

	private String toStatusLabel(String status) {
		if (!StringUtils.hasText(status)) {
			return null;
		}
		return switch (status) {
			case "draft" -> "Brouillon";
			case "posted" -> "Comptabilisée";
			case "cancel" -> "Annulée";
			default -> status;
		};
	}

	private String toPaymentStatusLabel(String paymentStatus) {
		if (!StringUtils.hasText(paymentStatus)) {
			return null;
		}
		return switch (paymentStatus) {
			case "not_paid" -> "Non payée";
			case "in_payment" -> "En paiement";
			case "partial" -> "Partiellement payée";
			case "paid" -> "Payée";
			case "reversed" -> "Extournée";
			default -> paymentStatus;
		};
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}

	private LocalDate asLocalDate(Object value) {
		String text = OdooValueMapper.asString(value);
		if (!StringUtils.hasText(text)) {
			return null;
		}
		try {
			return LocalDate.parse(text.substring(0, Math.min(10, text.length())));
		} catch (DateTimeParseException exception) {
			ZonedDateTime dateTime = OdooValueMapper.asZonedDateTime(value);
			return dateTime != null ? dateTime.toLocalDate() : null;
		}
	}

	private String toOdooDateTime(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(ODOO_DATE_TIME);
	}

	private String toOdooDate(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDate().format(ODOO_DATE);
	}
}
