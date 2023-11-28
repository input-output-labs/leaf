package fr.iolabs.leaf.payment.plan;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.SelectedPlanModule;

@Service
public class PlanService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;
	
	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafModuleService moduleService;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationRepository organizationRepository;
	
	public List<LeafPaymentPlan> fetchPlans() {
		return this.paymentConfig.getPlans();
	}

	private LeafPaymentPlan fetchPlanByName(String name) {
		for (LeafPaymentPlan plan : this.fetchPlans()) {
			if (plan.isAvailable() && name != null && name.equals(plan.getName())) {
				return plan;
			}
		}
		return null;
	}

	@Transactional
	public LeafPaymentPlan selectPlan(LeafPaymentPlan selectedPlan) {
		LeafPaymentPlan availableSelectedPlan = this.fetchPlanByName(selectedPlan.getName());
		if (availableSelectedPlan == null) {
			throw new BadRequestException("The given plan does not exist or is not available");
		}

		this.attachPaymentPlan(selectedPlan);
		this.savePlanAttachment();

		// This will update user or organization depending on selected plan
		this.applicationEventPublisher.publishEvent(new LeafPlanSelectionEvent(PlanService.class, availableSelectedPlan));

		return availableSelectedPlan;
	}
	
	public void attachPaymentPlan(LeafPaymentPlan plan) {
		PlanAttachment planAttachment = this.paymentConfig.getPlanAttachment();
		ILeafModular planOwnerTarget = null;
		switch (planAttachment) {
		case USER:
			planOwnerTarget = this.coreContext.getAccount();
			break;
		case ORGANIZATION:
			planOwnerTarget = this.coreContext.getOrganization();
			break;
		default:
			break;
		}
		
		this.attachPaymentPlanTo(plan, planOwnerTarget);
	}
	
	public void attachPaymentPlanTo(LeafPaymentPlan plan, ILeafModular planOwnerTarget) {
		SelectedPlanModule selectedPlanModule = this.getSelectedPlanModule(planOwnerTarget);
		selectedPlanModule.setSelectedPlan(plan);
		selectedPlanModule.getMetadata().updateLastModification();
	}

	private void savePlanAttachment() {
		switch (this.paymentConfig.getPlanAttachment()) {
		case USER:
			this.accountRepository.save(this.coreContext.getAccount());
			break;
		case ORGANIZATION:
			this.organizationRepository.save(this.coreContext.getOrganization());
			break;
		default:
			break;
		}
	}

	public SelectedPlanModule getSelectedPlanModule() {
		PlanAttachment planAttachment = this.paymentConfig.getPlanAttachment();
		ILeafModular planOwnerTarget = null;
		switch (planAttachment) {
		case USER:
			planOwnerTarget = this.coreContext.getAccount();
			break;
		case ORGANIZATION:
			planOwnerTarget = this.coreContext.getOrganization();
			break;
		default:
			break;
		}
		
		return this.getSelectedPlanModule(planOwnerTarget);
	}

	public SelectedPlanModule getSelectedPlanModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for selected plan module");
		}
		return this.moduleService.get(SelectedPlanModule.class, planOwnerTarget);
	}
}
