package fr.iolabs.leaf.odoo.invoice;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
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
	private static final String ACCOUNT_PAYMENT_MODEL = "account.payment";
	private static final int INVOICE_ID_CHUNK_SIZE = 100;
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter ODOO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final List<String> PAYMENT_DATE_FIELD_CANDIDATES = List.of(
		"payment_date",
		"date_last_payment",
		"last_payment_date",
		"invoice_date_paid"
	);
	private static final List<String> PAYMENT_INVOICE_LINK_FIELD_CANDIDATES = List.of(
		"reconciled_invoice_ids",
		"invoice_ids"
	);

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

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
			String paymentDateField = this.resolvePaymentDateField(availableMoveFields);

			List<Map<String, Object>> rows;
			if (paymentDateField != null) {
				List<Object> domain =
					this.resolvePaidInvoiceDomain(paymentDateField, fromInclusive, toInclusive, moveFields.get(paymentDateField));
				List<String> fields = this.resolveInvoiceFields(availableMoveFields, paymentDateField);
				rows = this.fetchAllInvoiceRows(credentials, uid, domain, fields, paymentDateField + " desc");
			} else {
				LOGGER.info("No payment date field on account.move, resolving paid invoices via account.payment");
				Set<Integer> invoiceIds =
					this.resolveInvoiceIdsPaidViaPayments(credentials, uid, fromInclusive, toInclusive);
				rows = this.readInvoicesByIds(credentials, uid, invoiceIds, availableMoveFields, null);
			}

			List<OdooInvoice> invoices = this.mapInvoices(credentials, rows, paymentDateField);
			LOGGER.info(
				"Listed {} Odoo invoices paid between {} and {} (db={})",
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
			List<OdooInvoiceLine> lines = this.mapInvoiceLines(rows, invoicesById);
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
			String paymentDateField = this.resolvePaymentDateField(availableMoveFields);

			List<Object> domain;
			List<String> fields = this.resolveInvoiceFields(availableMoveFields, paymentDateField);
			if (paymentDateField != null) {
				domain =
					this.resolveUnpaidDuringPeriodDomain(
						paymentDateField,
						toInclusive,
						moveFields.get(paymentDateField)
					);
			} else {
				domain = this.resolveUnpaidDuringPeriodDomainWithoutPaymentDate(toInclusive);
			}

			List<Map<String, Object>> rows = this.fetchAllInvoiceRows(credentials, uid, domain, fields, "create_date desc");
			List<OdooInvoice> invoices =
				this.mapInvoices(credentials, rows, paymentDateField)
					.stream()
					.filter(invoice -> this.isUnpaidDuringPeriod(invoice, toInclusive))
					.toList();

			LOGGER.info(
				"Listed {} Odoo invoices unpaid during period {} to {} (db={})",
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

	private String resolvePaymentDateField(Set<String> availableFields) {
		for (String candidate : PAYMENT_DATE_FIELD_CANDIDATES) {
			if (availableFields.contains(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	private List<Object> resolvePaidInvoiceDomain(
		String paymentDateField,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive,
		Object paymentDateFieldDefinition
	) {
		List<Object> domain = new ArrayList<>(this.basePostedCustomerInvoiceDomain());
		domain.add(List.of("payment_state", "in", List.of("paid", "partial")));
		domain.add(List.of(paymentDateField, "!=", false));
		domain.add(
			List.of(
				paymentDateField,
				">=",
				this.formatOdooFilterValue(paymentDateFieldDefinition, fromInclusive)
			)
		);
		domain.add(
			List.of(
				paymentDateField,
				"<=",
				this.formatOdooFilterValue(paymentDateFieldDefinition, toInclusive)
			)
		);
		return domain;
	}

	private List<Object> resolveUnpaidDuringPeriodDomain(
		String paymentDateField,
		ZonedDateTime periodEndInclusive,
		Object paymentDateFieldDefinition
	) {
		List<Object> domain = new ArrayList<>(this.basePostedCustomerInvoiceDomain());
		domain.add(
			List.of(
				"create_date",
				"<=",
				this.toOdooDateTime(periodEndInclusive)
			)
		);
		domain.add("|");
		domain.add(List.of("payment_state", "in", List.of("not_paid", "partial", "in_payment")));
		domain.add("&");
		domain.add(List.of("payment_state", "=", "paid"));
		domain.add(
			List.of(
				paymentDateField,
				">",
				this.formatOdooFilterValue(paymentDateFieldDefinition, periodEndInclusive)
			)
		);
		return domain;
	}

	private List<Object> resolveUnpaidDuringPeriodDomainWithoutPaymentDate(ZonedDateTime periodEndInclusive) {
		List<Object> domain = new ArrayList<>(this.basePostedCustomerInvoiceDomain());
		domain.add(List.of("create_date", "<=", this.toOdooDateTime(periodEndInclusive)));
		domain.add(List.of("payment_state", "in", List.of("not_paid", "partial", "in_payment", "paid")));
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

	private boolean isUnpaidDuringPeriod(OdooInvoice invoice, ZonedDateTime periodEndInclusive) {
		if (invoice.getCreatedAt() != null && invoice.getCreatedAt().isAfter(periodEndInclusive)) {
			return false;
		}

		if (!"paid".equals(invoice.getPaymentStatus())) {
			return true;
		}

		ZonedDateTime signedAt = invoice.getSignedAt();
		return signedAt == null || signedAt.isAfter(periodEndInclusive);
	}

	private Set<Integer> resolveInvoiceIdsPaidViaPayments(
		OdooCredentials credentials,
		int uid,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		Set<String> paymentFields =
			OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_PAYMENT_MODEL));
		String invoiceLinkField = this.resolvePaymentInvoiceLinkField(paymentFields);
		if (invoiceLinkField == null) {
			throw new OdooIntegrationException(
				"No invoice link field found on account.payment. Expected one of: " + PAYMENT_INVOICE_LINK_FIELD_CANDIDATES
			);
		}

		List<Object> domain = List.of(
			List.of("payment_type", "=", "inbound"),
			List.of("partner_type", "=", "customer"),
			List.of("state", "=", "posted"),
			List.of("date", ">=", this.toOdooDate(fromInclusive)),
			List.of("date", "<=", this.toOdooDate(toInclusive))
		);

		Set<Integer> invoiceIds = new HashSet<>();
		int offset = 0;
		while (true) {
			List<Map<String, Object>> page =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						ACCOUNT_PAYMENT_MODEL,
						domain,
						List.of("id", invoiceLinkField, "date"),
						PAGE_SIZE,
						offset,
						"date desc"
					);
			for (Map<String, Object> row : page) {
				invoiceIds.addAll(OdooValueMapper.asIntegerList(row.get(invoiceLinkField)));
			}
			if (page.size() < PAGE_SIZE) {
				break;
			}
			offset += page.size();
			if (offset >= MAX_TOTAL_RECORDS) {
				LOGGER.warn("Stopped Odoo payment pagination after {} records", offset);
				break;
			}
		}
		return invoiceIds;
	}

	private String resolvePaymentInvoiceLinkField(Set<String> availableFields) {
		for (String candidate : PAYMENT_INVOICE_LINK_FIELD_CANDIDATES) {
			if (availableFields.contains(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	private List<Map<String, Object>> readInvoicesByIds(
		OdooCredentials credentials,
		int uid,
		Set<Integer> invoiceIds,
		Set<String> availableMoveFields,
		String paymentDateField
	) {
		if (invoiceIds.isEmpty()) {
			return List.of();
		}
		List<String> fields = this.resolveInvoiceFields(availableMoveFields, paymentDateField);
		List<Integer> ids = new ArrayList<>(invoiceIds);
		List<Map<String, Object>> rows = new ArrayList<>();
		for (int offset = 0; offset < ids.size(); offset += PAGE_SIZE) {
			List<Integer> chunk = ids.subList(offset, Math.min(offset + PAGE_SIZE, ids.size()));
			rows.addAll(this.odooRpcClient.read(credentials, uid, ACCOUNT_MOVE_MODEL, chunk, fields));
		}
		return rows;
	}

	private List<String> resolveInvoiceFields(Set<String> availableFields, String paymentDateField) {
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		if (paymentDateField != null) {
			fields.add(paymentDateField);
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
		for (String candidate : Arrays.asList("price_subtotal", "display_type")) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private List<OdooInvoiceLine> mapInvoiceLines(
		List<Map<String, Object>> rows,
		Map<Integer, OdooInvoice> invoicesById
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

			OdooInvoiceLine line = new OdooInvoiceLine();
			line.setId(lineId);
			line.setInvoiceId(invoiceId);
			line.setInvoiceName(invoice.getName());
			line.setLabel(OdooValueMapper.asString(row.get("name")));
			line.setAmountUntaxed(amountUntaxed);
			line.setPaidAt(invoice.getSignedAt());
			lines.add(line);
		}
		return lines;
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
		String paymentDateField
	) {
		Map<Integer, OdooInvoice> uniqueById = new LinkedHashMap<>();
		for (Map<String, Object> row : rows) {
			Integer id = OdooValueMapper.asInteger(row.get("id"));
			if (id == null) {
				continue;
			}

			OdooInvoice invoice = new OdooInvoice();
			invoice.setId(id);
			invoice.setName(OdooValueMapper.asString(row.get("name")));
			invoice.setOdooUrl(this.buildInvoiceUrl(credentials, id));
			invoice.setCreatedAt(OdooValueMapper.asZonedDateTime(row.get("create_date")));
			invoice.setInvoiceDate(this.asLocalDate(row.get("invoice_date")));
			invoice.setDueDate(this.asLocalDate(row.get("invoice_date_due")));
			invoice.setSignedAt(
				paymentDateField != null ? OdooValueMapper.asZonedDateTime(row.get(paymentDateField)) : null
			);
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
			uniqueById.put(id, invoice);
		}
		return new ArrayList<>(uniqueById.values());
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

	private String formatOdooFilterValue(Object fieldDefinition, ZonedDateTime value) {
		if (fieldDefinition instanceof Map<?, ?> definitionMap && "date".equals(definitionMap.get("type"))) {
			return this.toOdooDate(value);
		}
		return this.toOdooDateTime(value);
	}

	private String toOdooDateTime(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().format(ODOO_DATE_TIME);
	}

	private String toOdooDate(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDate().format(ODOO_DATE);
	}
}
