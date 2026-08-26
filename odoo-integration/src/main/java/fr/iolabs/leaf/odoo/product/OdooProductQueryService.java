package fr.iolabs.leaf.odoo.product;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OdooProductQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooProductQueryService.class);
	private static final String PRODUCT_MODEL = "product.product";
	private static final int ID_CHUNK_SIZE = 100;
	private static final int DEFAULT_LIMIT = 50;
	private static final int MAX_LIMIT = 200;

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public List<OdooProduct> listProducts(Integer limit) {
		return this.listProducts(this.odooCredentialsResolver.resolveForApi(), limit);
	}

	public List<OdooProduct> listProducts(OdooCredentials credentials, Integer limit) {
		int safeLimit = sanitizeLimit(limit);
		try {
			int uid = this.odooRpcClient.authenticate(credentials);
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PRODUCT_MODEL));
			List<String> fields = this.resolveProductFields(availableFields, true);
			List<Object> domain = List.of(List.of("active", "=", true));
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(credentials, uid, PRODUCT_MODEL, domain, fields, safeLimit, "name asc");
			List<OdooProduct> products = new ArrayList<>();
			for (Map<String, Object> row : rows) {
				products.add(this.mapProduct(row, credentials, true));
			}
			LOGGER.debug("Listed {} Odoo products", products.size());
			return products;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to list Odoo products", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to list Odoo products", exception);
			throw new OdooIntegrationException("Failed to list Odoo products: " + exception.getMessage(), exception);
		}
	}

	public Map<Integer, OdooProduct> findByIds(OdooCredentials credentials, int uid, Set<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Map.of();
		}

		try {
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PRODUCT_MODEL));
			List<String> fields = this.resolveProductFields(availableFields, false);
			List<Integer> ids = new ArrayList<>(productIds);
			Map<Integer, OdooProduct> productsById = new LinkedHashMap<>();

			for (int offset = 0; offset < ids.size(); offset += ID_CHUNK_SIZE) {
				List<Integer> chunk = ids.subList(offset, Math.min(offset + ID_CHUNK_SIZE, ids.size()));
				List<Object> domain = List.of(List.of("id", "in", chunk));
				List<Map<String, Object>> rows =
					this.odooRpcClient.searchRead(credentials, uid, PRODUCT_MODEL, domain, fields, chunk.size(), 0, "id asc");
				for (Map<String, Object> row : rows) {
					OdooProduct product = this.mapProduct(row, credentials, false);
					if (product.getId() != null) {
						productsById.put(product.getId(), product);
					}
				}
			}

			LOGGER.debug("Resolved {} Odoo products from {} requested ids", productsById.size(), productIds.size());
			return productsById;
		} catch (OdooIntegrationException exception) {
			LOGGER.error("Failed to fetch Odoo products by ids", exception);
			throw exception;
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to fetch Odoo products by ids", exception);
			throw new OdooIntegrationException("Failed to fetch Odoo products: " + exception.getMessage(), exception);
		}
	}

	private List<String> resolveProductFields(Set<String> availableFields, boolean includeImage) {
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		for (String candidate : Arrays.asList("display_name", "default_code", "standard_price", "list_price")) {
			if (availableFields.contains(candidate)) {
				fields.add(candidate);
			}
		}
		if (includeImage && availableFields.contains("image_128")) {
			fields.add("image_128");
		}
		return fields;
	}

	private OdooProduct mapProduct(Map<String, Object> row, OdooCredentials credentials, boolean includeImage) {
		OdooProduct product = new OdooProduct();
		Integer id = OdooValueMapper.asInteger(row.get("id"));
		product.setId(id);
		String displayName = OdooValueMapper.asString(row.get("display_name"));
		product.setName(displayName != null ? displayName : OdooValueMapper.asString(row.get("name")));
		product.setDefaultCode(OdooValueMapper.asString(row.get("default_code")));
		product.setStandardPrice(this.asDouble(row.get("standard_price")));
		product.setListPrice(this.asDouble(row.get("list_price")));
		if (includeImage) {
			product.setImageUrl(this.resolveImageUrl(credentials, id, row.get("image_128")));
		}
		return product;
	}

	private String resolveImageUrl(OdooCredentials credentials, Integer productId, Object image128) {
		if (productId == null || !hasImage(image128)) {
			return null;
		}
		String base64 = asImageBase64(image128);
		if (base64 != null) {
			return "data:image/png;base64," + base64;
		}
		return credentials.getUrl() + "/web/image/product.product/" + productId + "/image_128";
	}

	private boolean hasImage(Object value) {
		if (value == null || Boolean.FALSE.equals(value)) {
			return false;
		}
		if (value instanceof byte[] bytes) {
			return bytes.length > 0;
		}
		String text = value.toString().trim();
		return !text.isEmpty() && !"false".equalsIgnoreCase(text) && !"0".equals(text) && !"0 bytes".equalsIgnoreCase(text);
	}

	private String asImageBase64(Object value) {
		if (value instanceof byte[] bytes && bytes.length > 0) {
			return Base64.getEncoder().encodeToString(bytes);
		}
		if (!(value instanceof String text)) {
			return null;
		}
		String trimmed = text.trim().replaceAll("\\s+", "");
		if (trimmed.isEmpty() || trimmed.matches("(?i)\\d+(\\.\\d+)?[kmg]?b")) {
			return null;
		}
		return trimmed;
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}

	private int sanitizeLimit(Integer limit) {
		int value = limit != null ? limit : DEFAULT_LIMIT;
		if (value <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(value, MAX_LIMIT);
	}

}
