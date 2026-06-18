package fr.iolabs.leaf.odoo.product;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.util.ArrayList;
import java.util.Arrays;
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

	@Autowired
	private OdooRpcClient odooRpcClient;

	public Map<Integer, OdooProduct> findByIds(OdooCredentials credentials, int uid, Set<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Map.of();
		}

		try {
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PRODUCT_MODEL));
			List<String> fields = this.resolveProductFields(availableFields);
			List<Integer> ids = new ArrayList<>(productIds);
			Map<Integer, OdooProduct> productsById = new LinkedHashMap<>();

			for (int offset = 0; offset < ids.size(); offset += ID_CHUNK_SIZE) {
				List<Integer> chunk = ids.subList(offset, Math.min(offset + ID_CHUNK_SIZE, ids.size()));
				List<Object> domain = List.of(List.of("id", "in", chunk));
				List<Map<String, Object>> rows =
					this.odooRpcClient.searchRead(credentials, uid, PRODUCT_MODEL, domain, fields, chunk.size(), 0, "id asc");
				for (Map<String, Object> row : rows) {
					OdooProduct product = this.mapProduct(row);
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

	private List<String> resolveProductFields(Set<String> availableFields) {
		List<String> fields = new ArrayList<>(List.of("id", "name"));
		if (availableFields.contains("standard_price")) {
			fields.add("standard_price");
		}
		return fields;
	}

	private OdooProduct mapProduct(Map<String, Object> row) {
		OdooProduct product = new OdooProduct();
		product.setId(OdooValueMapper.asInteger(row.get("id")));
		product.setName(OdooValueMapper.asString(row.get("name")));
		product.setStandardPrice(this.asDouble(row.get("standard_price")));
		return product;
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}

}
