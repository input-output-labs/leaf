package fr.iolabs.leaf.odoo.quote;

import java.util.ArrayList;
import java.util.List;

public class OdooCreateQuoteLineRequest {

	private Integer productId;
	private Double quantity;
	private Double unitPrice;

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
}
