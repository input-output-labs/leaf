package fr.iolabs.leaf.payment.models;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

@Deprecated
public class PaymentCustomerModule {
	private String stripeId;
	private PaymentMethod defaultPaymentMethod;
	private int freeTrialRemaining;
	private ResourceMetadata metadata;

	public PaymentCustomerModule() {
		this.freeTrialRemaining = -1;
		this.metadata = ResourceMetadata.create();
	}
	
	public String getStripeId() {
		return stripeId;
	}

	public void setStripeId(String stripeId) {
		this.stripeId = stripeId;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return metadata;
	}

	public int getFreeTrialRemaining() {
		return freeTrialRemaining;
	}

	public void setFreeTrialRemaining(int freeTrialRemaining) {
		this.freeTrialRemaining = freeTrialRemaining;
	}

	public void decreaseFreeTrialRemaining() {
		if (this.freeTrialRemaining > 0) {
			this.freeTrialRemaining--;
		}
	}

	public PaymentMethod getDefaultPaymentMethod() {
		return defaultPaymentMethod;
	}

	public void setDefaultPaymentMethod(PaymentMethod defaultPaymentMethod) {
		this.defaultPaymentMethod = defaultPaymentMethod;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}
}
