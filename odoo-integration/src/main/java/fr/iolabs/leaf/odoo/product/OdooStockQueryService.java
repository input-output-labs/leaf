package fr.iolabs.leaf.odoo.product;

import fr.iolabs.leaf.odoo.OdooCredentials;
import fr.iolabs.leaf.odoo.OdooCredentialsResolver;
import fr.iolabs.leaf.odoo.OdooIntegrationException;
import fr.iolabs.leaf.odoo.rpc.OdooRpcClient;
import fr.iolabs.leaf.odoo.rpc.OdooValueMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OdooStockQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdooStockQueryService.class);
	private static final String PRODUCT_MODEL = "product.product";
	private static final String ORDERPOINT_MODEL = "stock.warehouse.orderpoint";
	private static final int ID_CHUNK_SIZE = 100;

	@Autowired
	private OdooRpcClient odooRpcClient;

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	public List<OdooProductStock> findByProductIds(List<Integer> productIds) {
		List<Integer> ids = distinctIds(productIds);
		if (ids.isEmpty()) {
			return List.of();
		}

		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		int uid = this.odooRpcClient.authenticate(credentials);
		Map<Integer, OdooProductStock> stocks = this.readQuantities(credentials, uid, ids);
		this.applyMinimums(credentials, uid, ids, stocks);

		List<OdooProductStock> ordered = new ArrayList<>();
		for (Integer id : ids) {
			OdooProductStock stock = stocks.get(id);
			if (stock != null) {
				ordered.add(stock);
			}
		}
		return ordered;
	}

	private Map<Integer, OdooProductStock> readQuantities(
		OdooCredentials credentials,
		int uid,
		List<Integer> ids
	) {
		Set<String> availableFields =
			OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, PRODUCT_MODEL));
		if (!availableFields.contains("qty_available")) {
			throw new OdooIntegrationException("Odoo stock quantities are not available for this user");
		}

		List<String> fields = new ArrayList<>(List.of("id", "qty_available"));
		if (availableFields.contains("incoming_qty")) {
			fields.add("incoming_qty");
		}

		Map<Integer, OdooProductStock> stocks = new LinkedHashMap<>();
		for (int offset = 0; offset < ids.size(); offset += ID_CHUNK_SIZE) {
			List<Integer> chunk = ids.subList(offset, Math.min(offset + ID_CHUNK_SIZE, ids.size()));
			List<Object> domain = List.of(List.of("id", "in", chunk));
			List<Map<String, Object>> rows =
				this.odooRpcClient.searchRead(credentials, uid, PRODUCT_MODEL, domain, fields, chunk.size(), "id asc");
			for (Map<String, Object> row : rows) {
				Integer id = OdooValueMapper.asInteger(row.get("id"));
				if (id == null) {
					continue;
				}
				OdooProductStock stock = new OdooProductStock();
				stock.setProductId(id);
				stock.setQtyAvailable(asDouble(row.get("qty_available")));
				stock.setIncomingQty(asDouble(row.get("incoming_qty")));
				stocks.put(id, stock);
			}
		}
		LOGGER.debug("Read Odoo stock for {} of {} products", stocks.size(), ids.size());
		return stocks;
	}

	private void applyMinimums(
		OdooCredentials credentials,
		int uid,
		List<Integer> ids,
		Map<Integer, OdooProductStock> stocks
	) {
		if (stocks.isEmpty()) {
			return;
		}
		try {
			Set<String> availableFields =
				OdooValueMapper.toFieldNames(this.odooRpcClient.fieldsGet(credentials, uid, ORDERPOINT_MODEL));
			if (!availableFields.contains("product_min_qty") || !availableFields.contains("product_id")) {
				LOGGER.info("Odoo reordering rules do not expose a minimum quantity");
				return;
			}

			Map<Integer, List<Double>> minimumsByProduct = new LinkedHashMap<>();
			List<String> fields = List.of("product_id", "product_min_qty");
			for (int offset = 0; offset < ids.size(); offset += ID_CHUNK_SIZE) {
				List<Integer> chunk = ids.subList(offset, Math.min(offset + ID_CHUNK_SIZE, ids.size()));
				List<Object> domain = List.of(
					List.of("product_id", "in", chunk),
					List.of("active", "=", true)
				);
				List<Map<String, Object>> rows = this.odooRpcClient.searchRead(
					credentials,
					uid,
					ORDERPOINT_MODEL,
					domain,
					fields,
					Math.max(chunk.size() * 5, chunk.size()),
					"id asc"
				);
				for (Map<String, Object> row : rows) {
					Integer productId = OdooValueMapper.asMany2OneId(row.get("product_id"));
					Double minimum = asDouble(row.get("product_min_qty"));
					if (productId == null || minimum == null || !stocks.containsKey(productId)) {
						continue;
					}
					minimumsByProduct.computeIfAbsent(productId, ignored -> new ArrayList<>()).add(minimum);
				}
			}

			for (Map.Entry<Integer, List<Double>> entry : minimumsByProduct.entrySet()) {
				if (entry.getValue().size() == 1) {
					stocks.get(entry.getKey()).setMinQty(entry.getValue().get(0));
				}
			}
		} catch (OdooIntegrationException exception) {
			LOGGER.warn("Odoo reordering rules could not be read: {}", exception.getMessage());
		} catch (RuntimeException exception) {
			LOGGER.warn("Odoo reordering rules could not be read", exception);
		}
	}

	private List<Integer> distinctIds(List<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return List.of();
		}
		Set<Integer> ids = new LinkedHashSet<>();
		for (Integer id : productIds) {
			if (id != null && id > 0) {
				ids.add(id);
			}
		}
		return new ArrayList<>(ids);
	}

	private Double asDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return null;
	}
}
