package fr.iolabs.leaf.odoo.quote;

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
public class OdooQuoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooQuoteService.class);
	private static final String SALE_ORDER_MODEL = "sale.order";
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
			System.out.println("rows size : " + rows.size());
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
}
