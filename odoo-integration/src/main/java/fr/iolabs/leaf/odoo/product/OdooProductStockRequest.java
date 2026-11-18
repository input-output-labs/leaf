package fr.iolabs.leaf.odoo.product;

import java.util.ArrayList;
import java.util.List;

public class OdooProductStockRequest {

	private List<Integer> productIds = new ArrayList<>();

	public List<Integer> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<Integer> productIds) {
		this.productIds = productIds != null ? productIds : new ArrayList<>();
	}
}
