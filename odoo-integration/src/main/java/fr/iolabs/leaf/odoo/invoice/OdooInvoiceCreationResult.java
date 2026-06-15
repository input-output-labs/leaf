package fr.iolabs.leaf.odoo.invoice;

public class OdooInvoiceCreationResult {

	private OdooInvoiceCreationStatus status;
	private Integer invoiceId;
	private String invoiceUrl;
	private String documentName;
	private String errorMessage;
	private String details;

	public OdooInvoiceCreationStatus getStatus() {
		return status;
	}

	public void setStatus(OdooInvoiceCreationStatus status) {
		this.status = status;
	}

	public Integer getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Integer invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getInvoiceUrl() {
		return invoiceUrl;
	}

	public void setInvoiceUrl(String invoiceUrl) {
		this.invoiceUrl = invoiceUrl;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
