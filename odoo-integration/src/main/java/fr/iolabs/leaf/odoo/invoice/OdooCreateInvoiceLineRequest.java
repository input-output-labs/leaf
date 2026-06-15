package fr.iolabs.leaf.odoo.invoice;

public class OdooCreateInvoiceLineRequest {

	private String description;
	private Double quantity;
	private Double unitPrice;
	private Double vatPercent;
	private String comment;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Double getVatPercent() {
		return vatPercent;
	}

	public void setVatPercent(Double vatPercent) {
		this.vatPercent = vatPercent;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
