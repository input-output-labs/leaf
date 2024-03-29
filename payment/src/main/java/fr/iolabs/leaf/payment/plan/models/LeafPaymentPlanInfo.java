package fr.iolabs.leaf.payment.plan.models;

import fr.iolabs.leaf.payment.models.PaymentMethod;

public class LeafPaymentPlanInfo {
	private LeafPaymentPlan plan;
	private PaymentMethod paymentMethod;

	public LeafPaymentPlan getPlan() {
		return plan;
	}

	public void setPlan(LeafPaymentPlan plan) {
		this.plan = plan;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
