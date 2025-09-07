package fr.iolabs.leaf.payment.stripe.models;

public class PaymentIntentCancelAction {

	private String intentId;

	public String getIntentId() {
		return intentId;
	}

	public void setIntentId(String intentId) {
		this.intentId = intentId;
	}

}