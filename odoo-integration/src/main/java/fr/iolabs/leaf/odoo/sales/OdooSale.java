package fr.iolabs.leaf.odoo.sales;

import java.time.ZonedDateTime;

public class OdooSale {

	private Integer id;
	private String name;
	private String odooUrl;
	private ZonedDateTime createdAt;
	private ZonedDateTime dateOrder;
	private String status;
	private String statusLabel;
	private Integer partnerId;
	private String partnerName;
	private Double amountTotal;
	private Double amountUntaxed;
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

	public ZonedDateTime getDateOrder() {
		return dateOrder;
	}

	public void setDateOrder(ZonedDateTime dateOrder) {
		this.dateOrder = dateOrder;
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

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
}
