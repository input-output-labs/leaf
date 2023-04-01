package fr.iolabs.leaf.payment.stripe;

import java.util.List;

public class StripeCustomer {

	private String id;
	private List<String> paymentTransactionIds;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getPaymentTransactionIds() {
		return paymentTransactionIds;
	}
	public void setPaymentTransactionIds(List<String> paymentTransactionIds) {
		this.paymentTransactionIds = paymentTransactionIds;
	}
}
