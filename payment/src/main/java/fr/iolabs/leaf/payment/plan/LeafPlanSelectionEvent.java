package fr.iolabs.leaf.payment.plan;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

public class LeafPlanSelectionEvent extends ApplicationEvent  {
	private static final long serialVersionUID = 1L;

	private PlanAttachment planAttachment;
	private ILeafModular iModular;
	private LeafPaymentPlan selectedPlan;
	private LeafPaymentPlan previousPlan;

	public LeafPlanSelectionEvent(Object source, PlanAttachment planAttachment, ILeafModular iModular, LeafPaymentPlan selectedPlan, LeafPaymentPlan previousPlan) {
		super(source);
		this.planAttachment = planAttachment;
		this.iModular = iModular;
		this.selectedPlan = selectedPlan;
		this.previousPlan = previousPlan;
	}

	public LeafPaymentPlan getSelectedPlan() {
		return selectedPlan;
	}

	public PlanAttachment getPlanAttachment() {
		return planAttachment;
	}

	public ILeafModular getiModular() {
		return iModular;
	}

	public LeafPaymentPlan getPreviousPlan() {
		return previousPlan;
	}
}
