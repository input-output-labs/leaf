package fr.iolabs.leaf.payment.plan.models;

import java.util.List;

public class LeafPaymentPlan {
	private String name;
	private String color;
	private boolean available;
	private boolean defaultPlan;
	private List<LeafPaymentPlanFeature> features;
	private LeafPaymentPlanPricing pricing;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public boolean isDefaultPlan() {
		return defaultPlan;
	}

	public void setDefaultPlan(boolean defaultPlan) {
		this.defaultPlan = defaultPlan;
	}

	public List<LeafPaymentPlanFeature> getFeatures() {
		return features;
	}

	public void setFeatures(List<LeafPaymentPlanFeature> features) {
		this.features = features;
	}

	public LeafPaymentPlanPricing getPricing() {
		return pricing;
	}

	public void setPricing(LeafPaymentPlanPricing pricing) {
		this.pricing = pricing;
	}
}
