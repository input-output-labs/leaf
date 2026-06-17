package fr.iolabs.leaf.odoo.invoice;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class OdooInvoice {

	private Integer id;
	private String name;
	private String odooUrl;
	private ZonedDateTime createdAt;
	private LocalDate invoiceDate;
	private LocalDate dueDate;
	private ZonedDateTime signedAt;
	private String status;
	private String statusLabel;
	private String paymentStatus;
	private String paymentStatusLabel;
	private Integer partnerId;
	private String partnerName;
	private Double amountTotal;
	private Double amountUntaxed;
	private Double amountResidual;
	private String currencyCode;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOdooUrl() {
		return odooUrl;
	}

	public void setOdooUrl(String odooUrl) {
		this.odooUrl = odooUrl;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public ZonedDateTime getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(ZonedDateTime signedAt) {
		this.signedAt = signedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusLabel() {
		return statusLabel;
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentStatusLabel() {
		return paymentStatusLabel;
	}

	public void setPaymentStatusLabel(String paymentStatusLabel) {
		this.paymentStatusLabel = paymentStatusLabel;
	}

	public Integer getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public Double getAmountTotal() {
		return amountTotal;
	}

	public void setAmountTotal(Double amountTotal) {
		this.amountTotal = amountTotal;
	}

	public Double getAmountUntaxed() {
		return amountUntaxed;
	}

	public void setAmountUntaxed(Double amountUntaxed) {
		this.amountUntaxed = amountUntaxed;
	}

	public Double getAmountResidual() {
		return amountResidual;
	}

	public void setAmountResidual(Double amountResidual) {
		this.amountResidual = amountResidual;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
}
