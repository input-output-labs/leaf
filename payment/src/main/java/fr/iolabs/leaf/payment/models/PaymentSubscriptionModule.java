package fr.iolabs.leaf.payment.models;

import java.util.ArrayList;
import java.util.List;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentSubscription;

public class PaymentSubscriptionModule {
	private List<LeafPaymentSubscription> subscriptions;
	private ResourceMetadata metadata;

	public PaymentSubscriptionModule() {
		this.subscriptions = new ArrayList<>();
		this.metadata = ResourceMetadata.create();
	}

	public LeafPaymentSubscription findSubscription(String planId) {
		for (LeafPaymentSubscription subscription : subscriptions) {
			if (subscription.getAssociatedPlanId().equals(planId)) {
				return subscription;
			}
		}
		return null;
	}

	public List<LeafPaymentSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<LeafPaymentSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return metadata;
	}

	public void addSubscription(LeafPaymentSubscription leafPaymentSubscription) {
		this.subscriptions.add(leafPaymentSubscription);
	}

	public void setInactiveByPlanId(String planId) {
		for (LeafPaymentSubscription subscription : this.subscriptions) {
			if (planId.equals(subscription.getAssociatedPlanId())) {
				subscription.setActive(false);
			}
		}
	}
}
