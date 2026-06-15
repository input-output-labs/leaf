package fr.iolabs.leaf.odoo.invoice;

import java.time.LocalDate;
import java.util.List;

public class OdooCreateInvoiceRequest {

	private LocalDate issueDate;
	private LocalDate dueDate;
	private String contactId;
	private List<OdooCreateInvoiceLineRequest> lines;
	private String note;

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDate issueDate) {
		this.issueDate = issueDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public List<OdooCreateInvoiceLineRequest> getLines() {
		return lines;
	}

	public void setLines(List<OdooCreateInvoiceLineRequest> lines) {
		this.lines = lines;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
