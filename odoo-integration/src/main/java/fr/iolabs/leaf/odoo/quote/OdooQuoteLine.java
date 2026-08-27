package fr.iolabs.leaf.odoo.quote;

public class OdooQuoteLine {

	private Integer id;
	private Integer productId;
	private String productName;
	private String productDefaultCode;
	private Double quantity;
	private Double unitPrice;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDefaultCode() {
		return productDefaultCode;
	}

	public void setProductDefaultCode(String productDefaultCode) {
		this.productDefaultCode = productDefaultCode;
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
