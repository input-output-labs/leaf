package fr.iolabs.leaf.payment;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

public class PaymentModule {
	private String stripeCustomerId;
	private PaymentMethod defaultPaymentMethod;
	private int freeTrialRemaining;
	private LeafPaymentPlan selectedPlan;
	private ResourceMetadata metadata;

	public LeafPaymentPlan selectedPlan() {
		return selectedPlan;
	}

	public void setSelectedPlan(LeafPaymentPlan selectedPlan) {
		this.selectedPlan = selectedPlan;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return metadata;
	}

	public LeafPaymentPlan getSelectedPlan() {
		return selectedPlan;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public PaymentMethod getDefaultPaymentMethod() {
		return defaultPaymentMethod;
	}

	public void setDefaultPaymentMethod(PaymentMethod defaultPaymentMethod) {
		this.defaultPaymentMethod = defaultPaymentMethod;
	}

	public void decreaseFreeTrialRemaining() {
		if (this.freeTrialRemaining > 0) {
			this.freeTrialRemaining--;
		}
	}

	public int getFreeTrialRemaining() {
		return freeTrialRemaining;
	}

	public void setFreeTrialRemaining(int freeTrialRemaining) {
		this.freeTrialRemaining = freeTrialRemaining;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}
}
