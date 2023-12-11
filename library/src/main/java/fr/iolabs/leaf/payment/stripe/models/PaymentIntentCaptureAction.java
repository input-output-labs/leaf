package fr.iolabs.leaf.payment.stripe.models;

public class PaymentIntentCaptureAction {
	
	private String intentId;
	
	private Long amount;
	
	public String getIntentId() {
		return intentId;
	}

	public void setIntentId(String intentId) {
		this.intentId = intentId;
	}
	
	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

}
