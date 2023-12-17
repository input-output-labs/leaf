package fr.iolabs.leaf.payment.plan.models;

import fr.iolabs.leaf.common.LeafPolicy;

public class LeafPaymentPlanFeature extends LeafPolicy {
	
	public LeafPaymentPlanFeature clone() {
		LeafPaymentPlanFeature clone = new LeafPaymentPlanFeature();
		clone.name = this.name;
		clone.type = this.type;
		clone.value = this.value;
		return clone;
	}
}
