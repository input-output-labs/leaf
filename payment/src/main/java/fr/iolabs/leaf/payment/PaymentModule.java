package fr.iolabs.leaf.payment;

import java.util.ArrayList;
import java.util.List;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.services.LeafService;

public class PaymentModule {
	public static class ExtraServicePrice {
		public String stripePriceId;
		public LeafService service;
		public String period;
	}
	private String stripeCustomerId;
	private PaymentMethod defaultPaymentMethod;
	private int freeTrialRemaining;
	private LeafPaymentPlan selectedPlan;
	private List<ExtraServicePrice> extraServicePrices;
	private ResourceMetadata metadata;
	
	public PaymentModule() {
		this.setExtraServicePrices(new ArrayList<>());
	}

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

	public List<ExtraServicePrice> getExtraServicePrices() {
		return extraServicePrices;
	}

	public void setExtraServicePrices(List<ExtraServicePrice> extraServicePrices) {
		this.extraServicePrices = extraServicePrices;
	}

	public void removeExtraServicePrice(ExtraServicePrice extraServicePrice) {
		this.extraServicePrices = this.extraServicePrices.stream().filter(existingExtraServicePrice -> !existingExtraServicePrice.service.getKey().equals(extraServicePrice.service.getKey())).toList();
	}

	public void addExtraServicePrice(ExtraServicePrice extraServicePrice) {
		this.extraServicePrices = new ArrayList<>(this.extraServicePrices);
		this.extraServicePrices.add(extraServicePrice);
	}
}
