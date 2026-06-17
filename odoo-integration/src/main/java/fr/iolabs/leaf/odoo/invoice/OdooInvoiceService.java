package fr.iolabs.leaf.odoo.invoice;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OdooInvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooInvoiceService.class);
	private static final DateTimeFormatter ODOO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final String ACCOUNT_MOVE_MODEL = "account.move";
	private static final String ACCOUNT_TAX_MODEL = "account.tax";

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	@Autowired
	private OdooInvoiceQueryService odooInvoiceQueryService;

	public List<OdooInvoice> listInvoicesPaidBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.odooInvoiceQueryService.listInvoicesPaidBetween(fromInclusive, toInclusive);
	}

	public List<OdooInvoice> listInvoicesPaidBetween(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listInvoicesPaidBetween(organizationId, fromInclusive, toInclusive);
	}

	public List<OdooInvoice> listInvoicesPaidBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listInvoicesPaidBetween(credentials, fromInclusive, toInclusive);
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listInvoicesUnpaidDuringPeriod(fromInclusive, toInclusive);
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listInvoicesUnpaidDuringPeriod(organizationId, fromInclusive, toInclusive);
	}

	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listInvoicesUnpaidDuringPeriod(credentials, fromInclusive, toInclusive);
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(ZonedDateTime fromInclusive, ZonedDateTime toInclusive) {
		return this.odooInvoiceQueryService.listPaidInvoiceLinesBetween(fromInclusive, toInclusive);
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(
		String organizationId,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listPaidInvoiceLinesBetween(organizationId, fromInclusive, toInclusive);
	}

	public List<OdooInvoiceLine> listPaidInvoiceLinesBetween(
		OdooCredentials credentials,
		ZonedDateTime fromInclusive,
		ZonedDateTime toInclusive
	) {
		return this.odooInvoiceQueryService.listPaidInvoiceLinesBetween(credentials, fromInclusive, toInclusive);
	}

	public OdooInvoiceCreationResult createInvoice(OdooCreateInvoiceRequest request) {
		return this.createInvoice(this.odooCredentialsResolver.resolveFromCurrentOrganization(), request);
	}

	public OdooInvoiceCreationResult createInvoice(String organizationId, OdooCreateInvoiceRequest request) {
		return this.createInvoice(this.odooCredentialsResolver.resolveFromOrganizationId(organizationId), request);
	}

	public OdooInvoiceCreationResult createInvoice(OdooCredentials credentials, OdooCreateInvoiceRequest request) {
		try {
			LOGGER.info("Odoo invoice creation requested");
			this.validateRequest(request);
			LOGGER.info(
				"Odoo invoice request validated (contactId={}, issueDate={}, dueDate={}, lines={})",
				request.getContactId(),
				request.getIssueDate(),
				request.getDueDate(),
				request.getLines() != null ? request.getLines().size() : 0
			);

			int uid = this.odooRpcClient.authenticate(credentials);
			LOGGER.info("Odoo authentication succeeded (uid={})", uid);

			Integer partnerId = OdooValueMapper.asInteger(request.getContactId());
			if (partnerId == null || partnerId <= 0) {
				LOGGER.warn("Invalid Odoo contact id provided: {}", request.getContactId());
				return this.failure("Invalid contactId", "contactId must be a valid Odoo partner id");
			}
			LOGGER.info("Resolved Odoo partner id={}", partnerId);

			List<Object> invoiceLinesPayload = new ArrayList<>();
			for (int i = 0; i < request.getLines().size(); i++) {
				OdooCreateInvoiceLineRequest line = request.getLines().get(i);
				LOGGER.info(
					"Preparing invoice line {} (description={}, quantity={}, unitPrice={}, vatPercent={})",
					i + 1,
					line.getDescription(),
					line.getQuantity(),
					line.getUnitPrice(),
					line.getVatPercent()
				);
				Integer taxId = this.resolveSaleTaxIdByPercent(credentials, uid, line.getVatPercent());
				LOGGER.info("Line {} tax resolution result: vatPercent={} -> taxId={}", i + 1, line.getVatPercent(), taxId);
				Map<String, Object> lineValues = new HashMap<>();
				lineValues.put("name", this.buildLineLabel(line));
				lineValues.put("quantity", line.getQuantity());
				lineValues.put("price_unit", line.getUnitPrice());
				if (taxId != null) {
					lineValues.put("tax_ids", List.of(List.of(6, 0, List.of(taxId))));
				}
				invoiceLinesPayload.add(List.of(0, 0, lineValues));
			}

			Map<String, Object> moveValues = new HashMap<>();
			moveValues.put("move_type", "out_invoice");
			moveValues.put("partner_id", partnerId);
			moveValues.put("invoice_date", ODOO_DATE_FORMATTER.format(request.getIssueDate()));
			moveValues.put("invoice_date_due", ODOO_DATE_FORMATTER.format(request.getDueDate()));
			moveValues.put("invoice_line_ids", invoiceLinesPayload);
			if (StringUtils.hasText(request.getNote())) {
				moveValues.put("narration", request.getNote().trim());
			}
			this.applyOperationTypeSuppression(credentials, uid, moveValues);
			LOGGER.info(
				"Creating Odoo invoice (partnerId={}, issueDate={}, dueDate={}, lines={})",
				partnerId,
				moveValues.get("invoice_date"),
				moveValues.get("invoice_date_due"),
				invoiceLinesPayload.size()
			);

			int createdMoveId = this.odooRpcClient.createRecord(credentials, uid, ACCOUNT_MOVE_MODEL, moveValues);
			LOGGER.info("Odoo invoice created successfully (invoiceId={})", createdMoveId);

			Map<String, Object> invoiceData = this.readInvoice(credentials, uid, createdMoveId);
			String documentName = OdooValueMapper.asString(invoiceData.get("name"));
			String invoiceUrl =
				OdooValueMapper.ensureNoTrailingSlash(credentials.getUrl()) +
				"/my/invoices/" +
				createdMoveId +
				"?report_type=pdf&download=true";

			OdooInvoiceCreationResult result = new OdooInvoiceCreationResult();
			result.setStatus(OdooInvoiceCreationStatus.success);
			result.setInvoiceId(createdMoveId);
			result.setDocumentName(StringUtils.hasText(documentName) ? documentName : "Invoice #" + createdMoveId);
			result.setInvoiceUrl(invoiceUrl);
			result.setDetails("Invoice created successfully");
			LOGGER.info(
				"Odoo invoice creation completed (invoiceId={}, documentName={}, invoiceUrl={})",
				result.getInvoiceId(),
				result.getDocumentName(),
				result.getInvoiceUrl()
			);
			return result;
		} catch (Exception exception) {
			LOGGER.error("Failed to create Odoo invoice", exception);
			return this.failure("Failed to create Odoo invoice", exception.getMessage());
		}
	}

	private Map<String, Object> readInvoice(OdooCredentials credentials, int uid, int invoiceId) {
		LOGGER.info("Reading Odoo invoice details (invoiceId={})", invoiceId);
		List<Map<String, Object>> rows =
			this.odooRpcClient.read(
					credentials,
					uid,
					ACCOUNT_MOVE_MODEL,
					List.of(invoiceId),
					List.of("id", "name", "access_url")
				);
		return rows.isEmpty() ? Map.of() : rows.get(0);
	}

	private Integer resolveSaleTaxIdByPercent(OdooCredentials credentials, int uid, Double vatPercent) {
		if (vatPercent == null) {
			LOGGER.info("No VAT percent provided for line, tax will be omitted");
			return null;
		}
		double roundedVat = Math.round(vatPercent * 100d) / 100d;
		LOGGER.info("Resolving Odoo sale tax for vatPercent={}", roundedVat);
		List<Object> domain = List.of(
			List.of("type_tax_use", "=", "sale"),
			List.of("amount", "=", roundedVat),
			List.of("active", "=", true)
		);
		List<Map<String, Object>> rows =
			this.odooRpcClient.searchRead(
					credentials,
					uid,
					ACCOUNT_TAX_MODEL,
					domain,
					List.of("id"),
					1,
					"id asc"
				);
		if (!rows.isEmpty()) {
			return OdooValueMapper.asInteger(rows.get(0).get("id"));
		}
		LOGGER.warn("No Odoo sale tax found for vatPercent={}", roundedVat);
		return null;
	}

	private void applyOperationTypeSuppression(OdooCredentials credentials, int uid, Map<String, Object> moveValues) {
		Map<String, Object> fieldsDefinition = this.odooRpcClient.fieldsGet(credentials, uid, ACCOUNT_MOVE_MODEL);
		if (fieldsDefinition.containsKey("l10n_fr_operation_type")) {
			String serviceOperationValue = this.resolveServiceOperationTypeValue(fieldsDefinition.get("l10n_fr_operation_type"));
			if (StringUtils.hasText(serviceOperationValue)) {
				moveValues.put("l10n_fr_operation_type", serviceOperationValue);
				LOGGER.info(
					"Applied Odoo operation type as service using field l10n_fr_operation_type={}",
					serviceOperationValue
				);
			} else {
				LOGGER.warn(
					"Unable to resolve service value for l10n_fr_operation_type, keeping Odoo default operation type"
				);
			}
		}
	}

	private String resolveServiceOperationTypeValue(Object fieldDefinition) {
		if (!(fieldDefinition instanceof Map<?, ?> definitionMap)) {
			return null;
		}
		Object selection = definitionMap.get("selection");
		if (!(selection instanceof Object[] options)) {
			return null;
		}
		for (Object option : options) {
			if (option instanceof Object[] pair && pair.length >= 2) {
				String key = OdooValueMapper.asString(pair[0]);
				String label = OdooValueMapper.asString(pair[1]);
				if (this.looksLikeServiceOption(key, label)) {
					return key;
				}
			}
		}
		return null;
	}

	private boolean looksLikeServiceOption(String key, String label) {
		if (StringUtils.hasText(key) && key.toLowerCase(Locale.ROOT).contains("service")) {
			return true;
		}
		return StringUtils.hasText(label) && label.toLowerCase(Locale.ROOT).contains("service");
	}

	private String buildLineLabel(OdooCreateInvoiceLineRequest line) {
		String description = StringUtils.hasText(line.getDescription()) ? line.getDescription().trim() : "Line";
		if (StringUtils.hasText(line.getComment())) {
			return description + "\n" + line.getComment().trim();
		}
		return description;
	}

	private void validateRequest(OdooCreateInvoiceRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Invoice request is required");
		}
		if (request.getIssueDate() == null) {
			throw new IllegalArgumentException("issueDate is required");
		}
		if (request.getDueDate() == null) {
			throw new IllegalArgumentException("dueDate is required");
		}
		if (request.getDueDate().isBefore(request.getIssueDate())) {
			throw new IllegalArgumentException("dueDate cannot be before issueDate");
		}
		if (!StringUtils.hasText(request.getContactId())) {
			throw new IllegalArgumentException("contactId is required");
		}
		if (request.getLines() == null || request.getLines().isEmpty()) {
			throw new IllegalArgumentException("At least one invoice line is required");
		}
		for (int i = 0; i < request.getLines().size(); i++) {
			OdooCreateInvoiceLineRequest line = request.getLines().get(i);
			if (line == null) {
				throw new IllegalArgumentException("Line " + i + " is null");
			}
			if (!StringUtils.hasText(line.getDescription())) {
				throw new IllegalArgumentException("Line " + i + " description is required");
			}
			if (line.getQuantity() == null || line.getQuantity() <= 0d) {
				throw new IllegalArgumentException("Line " + i + " quantity must be > 0");
			}
			if (line.getUnitPrice() == null || line.getUnitPrice() < 0d) {
				throw new IllegalArgumentException("Line " + i + " unitPrice must be >= 0");
			}
			if (line.getVatPercent() == null || line.getVatPercent() < 0d) {
				throw new IllegalArgumentException("Line " + i + " vatPercent must be >= 0");
			}
		}
	}

	private OdooInvoiceCreationResult failure(String errorMessage, String details) {
		LOGGER.error("Odoo invoice creation failure: {} ({})", errorMessage, details);
		OdooInvoiceCreationResult result = new OdooInvoiceCreationResult();
		result.setStatus(OdooInvoiceCreationStatus.failure);
		result.setErrorMessage(errorMessage);
		result.setDetails(details);
		return result;
	}
}
