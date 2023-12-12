package fr.iolabs.leaf.payment.stripe.models;

import java.util.Map;

public class PaymentIntentData {

	private int applicationFeeAmount;

	private CaptureMethodEnum captureMethod;
	
	private String description;
	
	private String receiptEmail;
	
	private SetupFutureUsageEnum setupFutureUsage;
	
	private Map<String, Object> metadata;

	public int getApplicationFeeAmount() {
		return applicationFeeAmount;
	}

	public void setApplicationFeeAmount(int applicationFeeAmount) {
		this.applicationFeeAmount = applicationFeeAmount;
	}

	public CaptureMethodEnum getCaptureMethod() {
		return captureMethod;
	}

	public void setCaptureMethod(CaptureMethodEnum captureMethod) {
		this.captureMethod = captureMethod;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getReceiptEmail() {
		return receiptEmail;
	}

	public void setReceiptEmail(String receiptEmail) {
		this.receiptEmail = receiptEmail;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public SetupFutureUsageEnum getSetupFutureUsage() {
		return setupFutureUsage;
	}

	public void setSetupFutureUsage(SetupFutureUsageEnum setupFutureUsage) {
		this.setupFutureUsage = setupFutureUsage;
	}
}
