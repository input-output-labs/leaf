package fr.iolabs.leaf.payment.plan;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

public class LeafPlanSelectionEvent extends ApplicationEvent  {
	private static final long serialVersionUID = 1L;

	private LeafPaymentPlan selectedPlan;
	private Boolean planChangeAllowed;

	public LeafPlanSelectionEvent(Object source, LeafPaymentPlan selectedPlan) {
		super(source);
		this.selectedPlan = selectedPlan;
	}

	public LeafPaymentPlan getSelectedPlan() {
		return selectedPlan;
	}

	public Boolean getPlanChangeAllowed() {
		return planChangeAllowed;
	}

	public void setPlanChangeAllowed(Boolean planChangeAllowed) {
		this.planChangeAllowed = planChangeAllowed;
	}
}
