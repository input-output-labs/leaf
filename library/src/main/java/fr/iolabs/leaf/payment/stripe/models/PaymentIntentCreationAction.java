package fr.iolabs.leaf.payment.stripe.models;

import java.util.Map;

public class PaymentIntentCreationAction {

	private String currency;

	private Long amount;

	private boolean automaticPaymentMethods;

	private CaptureMethodEnum captureMethod;

	private Map<String, Object> metadata;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public boolean isAutomaticPaymentMethods() {
		return automaticPaymentMethods;
	}

	public void setAutomaticPaymentMethods(boolean automaticPaymentMethods) {
		this.automaticPaymentMethods = automaticPaymentMethods;
	}

	public CaptureMethodEnum getCaptureMethod() {
		return captureMethod;
	}

	public void setCaptureMethod(CaptureMethodEnum captureMethod) {
		this.captureMethod = captureMethod;
	}

}