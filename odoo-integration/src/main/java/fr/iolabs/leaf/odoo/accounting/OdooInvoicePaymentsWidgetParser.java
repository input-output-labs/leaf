package fr.iolabs.leaf.odoo.accounting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

public final class OdooInvoicePaymentsWidgetParser {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

	private OdooInvoicePaymentsWidgetParser() {}

	public static ZonedDateTime resolveLatestPaymentDate(Object widgetValue) {
		Map<String, Object> widget = parseWidget(widgetValue);
		if (widget == null) {
			return null;
		}

		Object content = widget.get("content");
		if (!(content instanceof List<?> payments)) {
			return null;
		}

		ZonedDateTime latestPaymentDate = null;
		for (Object payment : payments) {
			if (!(payment instanceof Map<?, ?> paymentMap)) {
				continue;
			}
			ZonedDateTime paymentDate = parsePaymentDate(paymentMap.get("date"));
			if (paymentDate == null) {
				continue;
			}
			if (latestPaymentDate == null || paymentDate.isAfter(latestPaymentDate)) {
				latestPaymentDate = paymentDate;
			}
		}
		return latestPaymentDate;
	}

	private static Map<String, Object> parseWidget(Object widgetValue) {
		if (widgetValue == null || widgetValue instanceof Boolean) {
			return null;
		}
		if (widgetValue instanceof Map<?, ?> map) {
			return OdooValueMapper.toStringKeyMap(map);
		}

		String text = OdooValueMapper.asString(widgetValue);
		if (!StringUtils.hasText(text)) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(text, MAP_TYPE);
		} catch (Exception exception) {
			return null;
		}
	}

	private static ZonedDateTime parsePaymentDate(Object value) {
		LocalDate date = asLocalDate(value);
		return date != null ? date.atStartOfDay(ZoneOffset.UTC) : null;
	}

	private static LocalDate asLocalDate(Object value) {
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
}
