package fr.iolabs.leaf.payment.plan.models;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeafPaymentPlan implements Cloneable {
	// For configuration file only
	private String parentName;
	// For configuration file only
	private boolean isParent;
	private String name;
	private String color;
	private String stripePriceId;
	private boolean available;
	private boolean defaultPlan;
	// "send_invoice" or "customer_default", if null, will return "customer_default"
	private String paymentMode;
	private List<LeafPaymentPlanFeature> features;
	private LeafPaymentPlanPricing pricing;
	private boolean suspended;
	private LeafPaymentPlan suspensionBackupPlan;
	private int trialDuration;
	private List<String> descriptions;
	private ZonedDateTime startedAt;
	private boolean inTrial;
	private String stripeSubscriptionId;
	private Map<String, String> metadata;
	
	public LeafPaymentPlan clone() {
		LeafPaymentPlan clone = new LeafPaymentPlan();
		clone.isParent = this.isParent;
		clone.parentName = this.parentName;
		clone.name = this.name;
		clone.color = this.color;
		clone.stripePriceId = this.stripePriceId;
		clone.available = this.available;
		clone.defaultPlan = this.defaultPlan;
		clone.descriptions = this.descriptions;
		clone.paymentMode = this.paymentMode;
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
		clone.trialDuration = this.trialDuration;
		clone.startedAt = this.startedAt;
		clone.inTrial = this.inTrial;
		clone.stripeSubscriptionId = this.stripeSubscriptionId;
		return clone;
	}

	public LeafPaymentPlan enrichedCloning(LeafPaymentPlan parent) {
		LeafPaymentPlan clone = new LeafPaymentPlan();
		clone.isParent = this.isParent;
		clone.parentName = this.parentName;
		clone.name = this.name;
		clone.available = this.available;
		clone.defaultPlan = this.defaultPlan;
		clone.paymentMode = this.paymentMode;
		clone.color = this.color != null ? this.color : parent.color;
		clone.stripePriceId = this.stripePriceId != null ? this.stripePriceId : parent.stripePriceId;
		clone.descriptions = this.descriptions != null ? this.descriptions : parent.descriptions;
		if (this.features != null) {
			clone.features = this.features.stream().map(feature -> feature.clone()).collect(Collectors.toList());
		} else if (parent.features != null) {
			clone.features = parent.features.stream().map(feature -> feature.clone()).collect(Collectors.toList());
		}
		if (this.pricing != null || parent.pricing != null) {
			clone.pricing = this.pricing.clone();
		} else if (parent.pricing != null) {
			clone.pricing = parent.pricing.clone();
		}
		clone.suspended = this.suspended;
		if (this.suspensionBackupPlan != null) {
			clone.suspensionBackupPlan = this.suspensionBackupPlan.clone();	
		}
		clone.trialDuration = this.trialDuration;
		clone.startedAt = this.startedAt;
		clone.inTrial = this.inTrial;
		clone.stripeSubscriptionId = this.stripeSubscriptionId;
		return clone;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public boolean getIsParent() {
		return isParent;
	}

	public void setIsParent(boolean isParent) {
		this.isParent = isParent;
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

	public ZonedDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(ZonedDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public boolean isInTrial() {
		return inTrial;
	}

	public void setInTrial(boolean inTrial) {
		this.inTrial = inTrial;
	}

	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}

	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}

	public int getTrialDuration() {
		return trialDuration;
	}

	public void setTrialDuration(int trialDuration) {
		this.trialDuration = trialDuration;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getPaymentMode() {
		if (paymentMode == null || paymentMode.isBlank()) {
			return "customer_default";
		}
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
}
