package fr.iolabs.leaf.payment.plan.models;

import java.util.List;
import java.util.stream.Collectors;

public class LeafPaymentPlan implements Cloneable {
	private String name;
	private String color;
	private String stripePriceId;
	private boolean available;
	private boolean defaultPlan;
	private List<LeafPaymentPlanFeature> features;
	private LeafPaymentPlanPricing pricing;
	private boolean suspended;
	private LeafPaymentPlan suspensionBackupPlan;
	private List<String> descriptions;
	
	public LeafPaymentPlan clone() {
		LeafPaymentPlan clone = new LeafPaymentPlan();
		clone.name = this.name;
		clone.color = this.color;
		clone.stripePriceId = this.stripePriceId;
		clone.available = this.available;
		clone.defaultPlan = this.defaultPlan;
		clone.descriptions = this.descriptions;
		if (this.features != null) {
			clone.features = this.features.stream().map(feature -> feature.clone()).collect(Collectors.toList());
		}
		if (this.pricing != null) {
			clone.pricing = this.pricing.clone();
		}
		clone.suspended = this.suspended;
		if (this.suspensionBackupPlan != null) {
			clone.suspensionBackupPlan = this.suspensionBackupPlan.clone();	
		}
		return clone;
	}

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

	public String getStripePriceId() {
		return stripePriceId;
	}

	public void setStripePriceId(String stripePriceId) {
		this.stripePriceId = stripePriceId;
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

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public LeafPaymentPlan getSuspensionBackupPlan() {
		return suspensionBackupPlan;
	}

	public void setSuspensionBackupPlan(LeafPaymentPlan suspensionBackupPlan) {
		this.suspensionBackupPlan = suspensionBackupPlan;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}
}
