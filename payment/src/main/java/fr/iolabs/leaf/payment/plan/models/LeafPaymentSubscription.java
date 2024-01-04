package fr.iolabs.leaf.payment.plan.models;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class LeafPaymentSubscription {
	private String stripeSubscriptionId;
	private String associatedPlanId;
	private boolean active;
	private ResourceMetadata metadata;

	public LeafPaymentSubscription(String stripeSubscriptionId, String associatedPlanId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
		this.associatedPlanId = associatedPlanId;
		this.getMetadata();
	}

	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}

	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}

	public String getAssociatedPlanId() {
		return associatedPlanId;
	}

	public void setAssociatedPlanId(String associatedPlanId) {
		this.associatedPlanId = associatedPlanId;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return metadata;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
