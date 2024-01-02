package fr.iolabs.leaf.payment.models;

import java.time.ZonedDateTime;

public class LeafInvoice {
	private String pdfUrl;
	private LeafPrice price;
	private String status;
	private boolean incoming;
	private ZonedDateTime creationDate;

	public String getPdfUrl() {
		return pdfUrl;
	}

	public LeafPrice getPrice() {
		return price;
	}

	public void setPrice(LeafPrice price) {
		this.price = price;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}

	public ZonedDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public void setPdfUrl(String pdfUrl) {
		this.pdfUrl = pdfUrl;
	}
}
