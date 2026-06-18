package fr.iolabs.leaf.odoo.accounting;

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
import java.util.HashMap;
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
public class OdooPaymentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooPaymentService.class);
	private static final String ACCOUNT_PAYMENT_MODEL = "account.payment";
	private static final int INVOICE_ID_CHUNK_SIZE = 100;
	private static final int PAGE_SIZE = 200;
	private static final int MAX_TOTAL_RECORDS = 10_000;
	private static final DateTimeFormatter ODOO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public Map<Integer, ZonedDateTime> resolveLatestPaymentDateByInvoiceId(
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.resolveLatestPaymentDateByInvoiceId(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			fromInclusive,
			toInclusive
		);
	}

	public Map<Integer, ZonedDateTime> resolveLatestPaymentDateByInvoiceId(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.resolveLatestPaymentDateByInvoiceId(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			fromInclusive,
			toInclusive
		);
	}

	public Map<Integer, ZonedDateTime> resolveLatestPaymentDateByInvoiceId(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		this.validatePeriod(fromInclusive, toInclusive);

		try {
			List<OdooPayment> payments =
				this.listInboundPostedPaymentsBetween(credentials, fromInclusive, toInclusive);
			Map<Integer, ZonedDateTime> paymentDatesByInvoiceId = new HashMap<>();
			for (OdooPayment payment : payments) {
				ZonedDateTime paymentDate = payment.getDate();
				if (paymentDate == null) {
					continue;
				}
				for (Integer invoiceId : payment.getReconciledInvoiceIds()) {
					paymentDatesByInvoiceId.merge(
						invoiceId,
						paymentDate,
						(existing, candidate) -> candidate.isAfter(existing) ? candidate : existing
					);
				}
			}
			LOGGER.info(
				"Resolved payment dates for {} Odoo invoices between {} and {} (db={})",
				paymentDatesByInvoiceId.size(),
				fromInclusive,
				toInclusive,
				credentials.getDb()
			);
			return paymentDatesByInvoiceId;
		} catch (OdooIntegrationException exception) {
			LOGGER.error(
				"Failed to resolve Odoo payment dates between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error(
				"Failed to resolve Odoo payment dates between {} and {}",
				fromInclusive,
				toInclusive,
				exception
			);
			throw new OdooIntegrationException(
				"Failed to resolve Odoo payment dates: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooPayment> listPaymentsReconciledWithInvoices(List<Integer> invoiceIds) {
		return this.listPaymentsReconciledWithInvoices(
			this.odooCredentialsResolver.resolveFromApplicationConfig(),
			invoiceIds
		);
	}

	public List<OdooPayment> listPaymentsReconciledWithInvoices(
		String organizationId,
		List<Integer> invoiceIds
	) {
		return this.listPaymentsReconciledWithInvoices(
			this.odooCredentialsResolver.resolveFromOrganizationId(organizationId),
			invoiceIds
		);
	}

	public List<OdooPayment> listPaymentsReconciledWithInvoices(
		OdooCredentials credentials,
		List<Integer> invoiceIds
	) {
		if (invoiceIds == null || invoiceIds.isEmpty()) {
			return List.of();
		}

		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_PAYMENT_MODEL));
			this.ensureReconciledInvoiceIdsField(availableFields);
			List<String> fields = this.resolvePaymentFields(availableFields);

			Map<Integer, OdooPayment> uniqueById = new LinkedHashMap<>();
			for (int offset = 0; offset < invoiceIds.size(); offset += INVOICE_ID_CHUNK_SIZE) {
				List<Integer> chunk = invoiceIds.subList(offset, Math.min(offset + INVOICE_ID_CHUNK_SIZE, invoiceIds.size()));
				List<Object> domain = this.resolvePaymentsForInvoicesDomain(availableFields, chunk);
				for (OdooPayment payment :
					this.mapPayments(this.fetchAllPaymentRows(credentials, uid, domain, fields))) {
					if (payment.getId() != null) {
						uniqueById.put(payment.getId(), payment);
					}
				}
			}

			List<OdooPayment> payments = new ArrayList<>(uniqueById.values());
			LOGGER.info(
				"Listed {} Odoo payments reconciled with {} invoice ids (db={})",
				payments.size(),
				invoiceIds.size(),
				credentials.getDb()
			);
			return payments;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo payments reconciled with invoice ids", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo payments reconciled with invoice ids", exception);
			throw new OdooIntegrationException(
				"Failed to list Odoo payments reconciled with invoices: " + exception.getMessage(),
				exception
			);
		}
	}

	public List<OdooPayment> listInboundPostedPaymentsBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		this.validatePeriod(fromInclusive, toInclusive);

		int uid = this.odooRpcClient.authenticate(credentials);
		Set<String> availableFields =
			OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_PAYMENT_MODEL));
		this.ensureReconciledInvoiceIdsField(availableFields);

		List<Object> domain = this.resolvePostedPaymentDomain(availableFields, fromInclusive, toInclusive);
		List<String> fields = this.resolvePaymentFields(availableFields);
		List<OdooPayment> payments =
			this.mapPayments(this.fetchAllPaymentRows(credentials, uid, domain, fields));
		LOGGER.info(
			"Listed {} inbound posted Odoo payments between {} and {} (db={})",
			payments.size(),
			fromInclusive,
			toInclusive,
			credentials.getDb()
		);
		return payments;
	}

	private void validatePeriod(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive is required");
		Objects.requireNonNull(toInclusive, "toInclusive is required");
		if (fromInclusive.isAfter(toInclusive)) {
			throw new IllegalArgumentException("fromInclusive must be before or equal to toInclusive");
		}
	}

	private void ensureReconciledInvoiceIdsField(Set<String> availableFields) {
		if (!availableFields.contains("reconciled_invoice_ids")) {
			throw new OdooIntegrationException(
				"Odoo model account.payment has no reconciled_invoice_ids field; cannot resolve invoice payment dates"
			);
		}
	}

	private List<Object> resolvePostedPaymentDomain(
		Set<String> availablePaymentFields,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		List<Object> domain = new ArrayList<>();
		domain.add(List.of("date", ">=", this.toOdooDate(fromInclusive)));
		domain.add(List.of("date", "<=", this.toOdooDate(toInclusive)));
		domain.add(List.of("reconciled_invoice_ids", "!=", false));
		if (availablePaymentFields.contains("state")) {
			domain.add(List.of("state", "=", "posted"));
		}
		if (availablePaymentFields.contains("payment_type")) {
			domain.add(List.of("payment_type", "=", "inbound"));
		}
		return domain;
	}

	private List<Object> resolvePaymentsForInvoicesDomain(Set<String> availablePaymentFields, List<Integer> invoiceIds) {
		List<Object> domain = new ArrayList<>();
		domain.add(List.of("reconciled_invoice_ids", "in", invoiceIds));
		if (availablePaymentFields.contains("state")) {
			domain.add(List.of("state", "=", "posted"));
		}
		if (availablePaymentFields.contains("payment_type")) {
			domain.add(List.of("payment_type", "=", "inbound"));
		}
		return domain;
	}

	private List<String> resolvePaymentFields(Set<String> availableFields) {
		List<String> fields = new ArrayList<>(List.of("id", "date", "reconciled_invoice_ids"));
		for (String candidate :
			Arrays.asList("name", "amount", "state", "payment_type", "ref", "journal_id")) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		return fields;
	}

	private List<OdooPayment> mapPayments(List<Map<String, Object>> rows) {
		List<OdooPayment> payments = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			Integer id = OdooValueMapper.asInteger(row.get("id"));
			if (id == null) {
				continue;
			}

			OdooPayment payment = new OdooPayment();
			payment.setId(id);
			payment.setName(OdooValueMapper.asString(row.get("name")));
			payment.setDate(this.asPaymentDate(row.get("date")));
			payment.setAmount(this.asDouble(row.get("amount")));
			payment.setState(OdooValueMapper.asString(row.get("state")));
			payment.setPaymentType(OdooValueMapper.asString(row.get("payment_type")));
			payment.setRef(OdooValueMapper.asString(row.get("ref")));
			payment.setJournalName(OdooValueMapper.asMany2OneDisplayName(row.get("journal_id")));
			payment.setReconciledInvoiceIds(OdooValueMapper.asIntegerList(row.get("reconciled_invoice_ids")));
			payments.add(payment);
		}
		return payments;
	}

	private List<Map<String, Object>> fetchAllPaymentRows(
		OdooCredentials credentials,
		int uid,
		List<Object> domain,
		List<String> fields
	) {
		List<Map<String, Object>> allRows = new ArrayList<>();
		int offset = 0;
		int pageNumber = 0;

		while (true) {
			List<Map<String, Object>> page =
				this.odooRpcClient.searchRead(
						credentials,
						uid,
						ACCOUNT_PAYMENT_MODEL,
						domain,
						fields,
						PAGE_SIZE,
						offset,
						"date asc, id asc"
					);
			pageNumber++;
			allRows.addAll(page);
			LOGGER.debug(
				"Fetched Odoo payments page {} (offset={}, pageSize={}, totalSoFar={})",
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
				LOGGER.warn("Stopped Odoo payment pagination after {} records", offset);
				break;
			}
		}

		return allRows;
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}

	private ZonedDateTime asPaymentDate(Object value) {
		LocalDate date = this.asLocalDate(value);
		return date != null ? date.atStartOfDay(ZoneOffset.UTC) : null;
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

	private String toOdooDate(ZonedDateTime value) {
		return value.withZoneSameInstant(ZoneOffset.UTC).toLocalDate().format(ODOO_DATE);
	}
}
