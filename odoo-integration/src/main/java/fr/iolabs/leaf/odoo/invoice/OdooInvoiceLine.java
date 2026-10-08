package fr.iolabs.leaf.odoo.invoice;

import java.time.ZonedDateTime;

public class OdooInvoiceLine {

	private Integer id;
	private Integer invoiceId;
	private String invoiceName;
	private String label;
	private Double amountUntaxed;
	private Integer productId;
	private Double quantity;
	private Double unitPrice;
	private String productName;
	private String productDefaultCode;
	private Double unitCost;
	private Double totalCost;
	private ZonedDateTime paidAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Integer invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getInvoiceName() {
		return invoiceName;
	}

	public void setInvoiceName(String invoiceName) {
		this.invoiceName = invoiceName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getAmountUntaxed() {
		return amountUntaxed;
	}

	public void setAmountUntaxed(Double amountUntaxed) {
		this.amountUntaxed = amountUntaxed;
	}

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

	public Double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(Double unitCost) {
		this.unitCost = unitCost;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public ZonedDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(ZonedDateTime paidAt) {
		this.paidAt = paidAt;
	}
}
