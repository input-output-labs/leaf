package fr.iolabs.leaf.payment.stripe.models;

public class InvoiceItemCreationAction {
	private String description;
	private long amount;
	private String currency;
	/** VAT ratio (e.g. {@code 0.2} for 20%). When set, a Stripe tax rate is applied on the invoice line. */
	private Double vatRatio;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getVatRatio() {
		return vatRatio;
	}

	public void setVatRatio(Double vatRatio) {
		this.vatRatio = vatRatio;
	}
}
