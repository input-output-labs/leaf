package fr.iolabs.leaf.payment.plan.models;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

@Deprecated
public class SelectedPlanModule {
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
}
