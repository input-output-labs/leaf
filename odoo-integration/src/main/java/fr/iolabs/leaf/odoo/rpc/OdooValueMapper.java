package fr.iolabs.leaf.odoo.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

public final class OdooValueMapper {

	private OdooValueMapper() {}

	public static List<Map<String, Object>> toRecordList(Object xmlRpcResult) {
		List<Map<String, Object>> records = new ArrayList<>();
		if (xmlRpcResult instanceof Object[] array) {
			for (Object entry : array) {
				addRecord(records, entry);
			}
		} else if (xmlRpcResult instanceof List<?> list) {
			for (Object entry : list) {
				addRecord(records, entry);
			}
		}
		return records;
	}

	public static Map<String, Object> toRecord(Object xmlRpcResult) {
		List<Map<String, Object>> records = toRecordList(xmlRpcResult);
		return records.isEmpty() ? Map.of() : records.get(0);
	}

	public static Set<String> toFieldNames(Object fieldsGetResponse) {
		if (!(fieldsGetResponse instanceof Map<?, ?> fieldsMap)) {
			return Set.of();
		}
		Set<String> result = new HashSet<>();
		for (Object key : fieldsMap.keySet()) {
			if (key != null) {
				result.add(key.toString());
			}
		}
		return result;
	}

	public static Map<String, Object> toStringKeyMap(Object rawMap) {
		Map<String, Object> converted = new HashMap<>();
		if (!(rawMap instanceof Map<?, ?> source)) {
			return converted;
		}
		for (Map.Entry<?, ?> entry : source.entrySet()) {
			if (entry.getKey() != null) {
				converted.put(entry.getKey().toString(), entry.getValue());
			}
		}
		return converted;
	}

	public static String asString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean bool) {
			return bool ? "true" : null;
		}
		String text = value.toString().trim();
		if (text.isEmpty() || "false".equalsIgnoreCase(text) || "null".equalsIgnoreCase(text)) {
			return null;
		}
		return text;
	}

	public static String asMany2OneDisplayName(Object value) {
		if (value == null || value instanceof Boolean) {
			return null;
		}
		if (value instanceof Object[] array) {
			if (array.length > 1 && array[1] != null) {
				return asString(array[1]);
			}
			if (array.length > 0 && array[0] != null) {
				return asString(array[0]);
			}
			return null;
		}
		if (value instanceof List<?> list) {
			if (list.size() > 1 && list.get(1) != null) {
				return asString(list.get(1));
			}
			if (!list.isEmpty() && list.get(0) != null) {
				return asString(list.get(0));
			}
			return null;
		}
		return asString(value);
	}

	public static Integer asInteger(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value == null) {
			return null;
		}
		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	public static Integer asMany2OneId(Object value) {
		if (value == null || value instanceof Boolean) {
			return null;
		}
		if (value instanceof Object[] array && array.length > 0) {
			return asInteger(array[0]);
		}
		if (value instanceof List<?> list && !list.isEmpty()) {
			return asInteger(list.get(0));
		}
		return asInteger(value);
	}

	public static List<Integer> asIntegerList(Object value) {
		List<Integer> result = new ArrayList<>();
		if (value instanceof Object[] array) {
			for (Object item : array) {
				Integer id = asInteger(item);
				if (id != null) {
					result.add(id);
				}
			}
		} else if (value instanceof List<?> list) {
			for (Object item : list) {
				Integer id = asInteger(item);
				if (id != null) {
					result.add(id);
				}
			}
		} else {
			Integer singleValue = asInteger(value);
			if (singleValue != null) {
				result.add(singleValue);
			}
		}
		return result;
	}

	public static Integer extractCreatedId(Object raw) {
		Integer directValue = asInteger(raw);
		if (directValue != null && directValue > 0) {
			return directValue;
		}
		if (raw instanceof Object[] array && array.length > 0) {
			Integer firstArrayValue = asInteger(array[0]);
			if (firstArrayValue != null && firstArrayValue > 0) {
				return firstArrayValue;
			}
		}
		if (raw instanceof List<?> list && !list.isEmpty()) {
			Integer firstListValue = asInteger(list.get(0));
			if (firstListValue != null && firstListValue > 0) {
				return firstListValue;
			}
		}
		if (raw instanceof Map<?, ?> map) {
			Integer mapId = asInteger(map.get("id"));
			if (mapId != null && mapId > 0) {
				return mapId;
			}
		}
		return null;
	}

	public static String ensureNoTrailingSlash(String value) {
		String trimmed = value.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	public static void putIfHasText(Map<String, Object> values, String key, String value) {
		if (StringUtils.hasText(value)) {
			values.put(key, value.trim());
		}
	}

	private static void addRecord(List<Map<String, Object>> records, Object entry) {
		if (entry instanceof Map<?, ?> row) {
			records.add(toStringKeyMap(row));
		}
	}
}
