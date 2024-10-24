package fr.iolabs.leaf.payment.stripe.models;

import java.util.List;

public class InvoiceCreationAction {
	private String description;
	private List<InvoiceItemCreationAction> items;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<InvoiceItemCreationAction> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItemCreationAction> items) {
		this.items = items;
	}
}
