package fr.iolabs.leaf.odoo.invoice;

import java.time.ZonedDateTime;

public class OdooInvoiceLine {

	private Integer id;
	private Integer invoiceId;
	private String invoiceName;
	private String label;
	private Double amountUntaxed;
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

	public ZonedDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(ZonedDateTime paidAt) {
		this.paidAt = paidAt;
	}
}
