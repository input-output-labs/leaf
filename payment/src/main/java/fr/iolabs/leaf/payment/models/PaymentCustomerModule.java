package fr.iolabs.leaf.payment.models;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class PaymentCustomerModule {
	private String stripeId;
	private PaymentMethod defaultPaymentMethod;
	private ResourceMetadata metadata;

	public PaymentCustomerModule() {
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
