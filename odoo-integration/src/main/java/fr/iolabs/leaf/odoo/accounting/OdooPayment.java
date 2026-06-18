package fr.iolabs.leaf.odoo.accounting;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class OdooPayment {

	private Integer id;
	private String name;
	private ZonedDateTime date;
	private Double amount;
	private String state;
	private String paymentType;
	private String ref;
	private String journalName;
	private List<Integer> reconciledInvoiceIds = new ArrayList<>();

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

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getJournalName() {
		return journalName;
	}

	public void setJournalName(String journalName) {
		this.journalName = journalName;
	}

	public List<Integer> getReconciledInvoiceIds() {
		return reconciledInvoiceIds;
	}

	public void setReconciledInvoiceIds(List<Integer> reconciledInvoiceIds) {
		this.reconciledInvoiceIds = reconciledInvoiceIds != null ? reconciledInvoiceIds : new ArrayList<>();
	}
}
