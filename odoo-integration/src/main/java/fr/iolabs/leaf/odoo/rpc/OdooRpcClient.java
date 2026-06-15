package fr.iolabs.leaf.odoo.rpc;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class OdooRpcClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooRpcClient.class);

	private final RestTemplate restTemplate = new RestTemplate();
	private final AtomicInteger requestId = new AtomicInteger(1);

	public int authenticate(OdooCredentials credentials) {
		Object result = call(
			credentials,
			"common",
			"authenticate",
			List.of(credentials.getDb(), credentials.getUsername(), credentials.getPassword(), Map.of())
		);
		if (result instanceof Number number) {
			int uid = number.intValue();
			if (uid <= 0) {
				throw new OdooIntegrationException("Odoo authentication failed for user " + credentials.getUsername());
			}
			return uid;
		}
		if (result instanceof Boolean bool && !bool) {
			throw new OdooIntegrationException("Odoo authentication failed for user " + credentials.getUsername());
		}
		throw new OdooIntegrationException("Unexpected Odoo authentication response");
	}

	public int createRecord(OdooCredentials credentials, int uid, String model, Map<String, Object> values) {
		Object result = executeKw(credentials, uid, model, "create", List.of(values), Map.of());
		Integer createdId = OdooValueMapper.extractCreatedId(result);
		if (createdId == null || createdId <= 0) {
			throw new OdooIntegrationException("Unexpected Odoo create response for model " + model);
		}
		return createdId;
	}

	public void writeRecord(OdooCredentials credentials, int uid, String model, int recordId, Map<String, Object> values) {
		if (values == null || values.isEmpty()) {
			return;
		}
		Object result = executeKw(
			credentials,
			uid,
			model,
			"write",
			List.of(List.of(recordId), values),
			Map.of()
		);
		if (!(result instanceof Boolean bool) || !bool) {
			throw new OdooIntegrationException("Unexpected Odoo write response for model " + model);
		}
	}

	public List<Map<String, Object>> searchRead(
		OdooCredentials credentials,
		int uid,
		String model,
		List<?> domain,
		List<String> fields,
		int limit,
		String order
	) {
		return this.searchRead(credentials, uid, model, domain, fields, limit, 0, order);
	}

	public List<Map<String, Object>> searchRead(
		OdooCredentials credentials,
		int uid,
		String model,
		List<?> domain,
		List<String> fields,
		int limit,
		int offset,
		String order
	) {
		Map<String, Object> kwargs = new HashMap<>();
		kwargs.put("fields", fields);
		kwargs.put("limit", limit);
		kwargs.put("offset", offset);
		if (order != null) {
			kwargs.put("order", order);
		}
		Object result = executeKw(credentials, uid, model, "search_read", List.of(domain), kwargs);
		return OdooValueMapper.toRecordList(result);
	}

	public List<Map<String, Object>> read(
		OdooCredentials credentials,
		int uid,
		String model,
		List<Integer> ids,
		List<String> fields
	) {
		Map<String, Object> kwargs = new HashMap<>();
		kwargs.put("fields", fields);
		Object result = executeKw(credentials, uid, model, "read", List.of(ids), kwargs);
		return OdooValueMapper.toRecordList(result);
	}

	public Map<String, Object> fieldsGet(OdooCredentials credentials, int uid, String model) {
		Object result = executeKw(credentials, uid, model, "fields_get", List.of(), Map.of());
		return OdooValueMapper.toStringKeyMap(result);
	}

	public Object executeKw(
		OdooCredentials credentials,
		int uid,
		String model,
		String method,
		List<Object> args,
		Map<String, Object> kwargs
	) {
		if (kwargs == null || kwargs.isEmpty()) {
			return call(
				credentials,
				"object",
				"execute_kw",
				List.of(credentials.getDb(), uid, credentials.getPassword(), model, method, args)
			);
		}
		return call(
			credentials,
			"object",
			"execute_kw",
			List.of(credentials.getDb(), uid, credentials.getPassword(), model, method, args, kwargs)
		);
	}

	private Object call(OdooCredentials credentials, String service, String method, List<Object> args) {
		String url = credentials.getUrl() + "/jsonrpc";
		Map<String, Object> body = new HashMap<>();
		body.put("jsonrpc", "2.0");
		body.put("method", "call");
		body.put("id", requestId.getAndIncrement());
		body.put("params", Map.of("service", service, "method", method, "args", args));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		LOGGER.debug("Odoo RPC {}.{} on {}", service, method, url);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
			Map<String, Object> responseBody = response.getBody();
			if (responseBody == null) {
				throw new OdooIntegrationException("Empty Odoo RPC response");
			}
			if (responseBody.containsKey("error")) {
				throw new OdooIntegrationException(formatRpcError(responseBody.get("error")));
			}
			return responseBody.get("result");
		} catch (RestClientException exception) {
			throw new OdooIntegrationException("Failed to call Odoo RPC: " + exception.getMessage(), exception);
		}
	}

	private String formatRpcError(Object errorObject) {
		if (!(errorObject instanceof Map<?, ?> error)) {
			return "Unknown Odoo RPC error";
		}
		Object message = error.get("message");
		Object data = error.get("data");
		if (data instanceof Map<?, ?> dataMap) {
			Object debug = dataMap.get("debug");
			if (debug != null) {
				return String.valueOf(message) + " — " + debug;
			}
			Object name = dataMap.get("name");
			if (name != null) {
				return String.valueOf(message) + " — " + name;
			}
		}
		return message != null ? message.toString() : "Unknown Odoo RPC error";
	}
}
