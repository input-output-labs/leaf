package fr.iolabs.leaf.payment.stripe.models;

public class PaymentIntentRefundAction {

	private String intentId;

	// The amount to refund in cent (EUR * 100)
	private Long refundedAmount;

	public String getIntentId() {
		return intentId;
	}

	public void setIntentId(String intentId) {
		this.intentId = intentId;
	}

	public Long getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(Long refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

}