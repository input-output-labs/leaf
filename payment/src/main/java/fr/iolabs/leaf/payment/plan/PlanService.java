package fr.iolabs.leaf.payment.plan;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.models.PaymentSubscriptionModule;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanInfo;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentSubscription;
import fr.iolabs.leaf.payment.plan.models.SelectedPlanModule;
import fr.iolabs.leaf.payment.stripe.StripeSubcriptionService;

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
	private StripeSubcriptionService stripeSubcriptionService;

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
		availableSelectedPlan.setSuspensionBackupPlan(this.paymentConfig.getDefaultPlan());

		LeafPaymentPlan previousPlan = this.getCurrentPlan();
		this.attachPaymentPlan(availableSelectedPlan);
		if (!availableSelectedPlan.getPricing().isFree()) {
			this.createPaymentSubscription(availableSelectedPlan);
		}
		if (previousPlan != null && !previousPlan.getPricing().isFree()) {
			this.revokePaymentSubscription(previousPlan);
		}
		this.savePlanAttachment();

		// This will update user or organization depending on selected plan
		this.applicationEventPublisher.publishEvent(new LeafPlanSelectionEvent(PlanService.class, availableSelectedPlan));

		return availableSelectedPlan;
	}

	private LeafPaymentPlan getCurrentPlan() {
		SelectedPlanModule selectedPlan =  this.getSelectedPlanModule();
		return selectedPlan.getSelectedPlan();
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

	private ILeafModular savePlanAttachment(ILeafModular iModular) {
		switch (this.paymentConfig.getPlanAttachment()) {
		case USER:
			if (iModular instanceof LeafAccount) {
				return this.accountRepository.save((LeafAccount) iModular);
			}
			break;
		case ORGANIZATION:
			if (iModular instanceof LeafOrganization) {
				return this.organizationRepository.save((LeafOrganization) iModular);
			}
			break;
		default:
			break;
		}
		return null;
	}

	public ILeafModular getPlanAttachement() {
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
		
		return planOwnerTarget;
	}

	public ILeafModular getPlanAttachement(String iModularId) {
		PlanAttachment planAttachment = this.paymentConfig.getPlanAttachment();
		ILeafModular planOwnerTarget = null;
		switch (planAttachment) {
		case USER:
			Optional<LeafAccount> opt = this.accountRepository.findById(iModularId);
			if (opt.isPresent()) {
				planOwnerTarget = opt.get();
			}
			break;
		case ORGANIZATION:
			Optional<LeafOrganization> optOrg = this.organizationRepository.findById(iModularId);
			if (optOrg.isPresent()) {
				planOwnerTarget = optOrg.get();
			}
			break;
		default:
			break;
		}
		
		return planOwnerTarget;
	}

	public SelectedPlanModule getSelectedPlanModule() {
		ILeafModular planOwnerTarget = this.getPlanAttachement();
		return this.getSelectedPlanModule(planOwnerTarget);
	}

	public SelectedPlanModule getSelectedPlanModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for selected plan module");
		}
		return this.moduleService.get(SelectedPlanModule.class, planOwnerTarget);
	}

	public PaymentSubscriptionModule getPaymentSubscriptionModule() {
		ILeafModular planOwnerTarget = this.getPlanAttachement();
		return this.getPaymentSubscriptionModule(planOwnerTarget);
	}

	public PaymentSubscriptionModule getPaymentSubscriptionModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for payment subscription module");
		}
		return this.moduleService.get(PaymentSubscriptionModule.class, planOwnerTarget);
	}

	public PaymentCustomerModule getPaymentCustomerModule() {
		ILeafModular planOwnerTarget = this.getPlanAttachement();
		return this.getPaymentCustomerModule(planOwnerTarget);
	}

	public PaymentCustomerModule getPaymentCustomerModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for payment customer module");
		}
		return this.moduleService.get(PaymentCustomerModule.class, planOwnerTarget);
	}
	
	private void createPaymentSubscription(LeafPaymentPlan selectedPlan) {
		PaymentSubscriptionModule subscription = this.getPaymentSubscriptionModule();
		PaymentCustomerModule customer = this.getPaymentCustomerModule();
		try {
			this.stripeSubcriptionService.createSubscription(customer, subscription, selectedPlan);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot create plan subscription");
		}
	}
	
	private void revokePaymentSubscription(LeafPaymentPlan selectedPlan) {
		PaymentSubscriptionModule subscription = this.getPaymentSubscriptionModule();
		PaymentCustomerModule customer = this.getPaymentCustomerModule();
		try {
			this.stripeSubcriptionService.revokeSubscription(customer, subscription, selectedPlan);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot create plan subscription");
		}
	}

	public LeafPaymentPlanInfo getSelectedPlan() {
		LeafPaymentPlanInfo paymentPlanInfo = new LeafPaymentPlanInfo();
		
		SelectedPlanModule selectedPlan = this.getSelectedPlanModule();
		PaymentSubscriptionModule paymentSubscription = this.getPaymentSubscriptionModule();
		PaymentCustomerModule paymentCustomer= this.getPaymentCustomerModule();
		
		if (selectedPlan.getSelectedPlan() != null) {
			paymentPlanInfo.setPlan(selectedPlan.getSelectedPlan());
			LeafPaymentSubscription subscription = paymentSubscription.findSubscription(selectedPlan.getSelectedPlan().getName());
			if (subscription != null) {
				paymentPlanInfo.setTrialDone(subscription.isTrialDone());
			}
		}
		
		if (paymentCustomer != null) {
			paymentPlanInfo.setPaymentMethod(paymentCustomer.getDefaultPaymentMethod());
		}
		
		return paymentPlanInfo;
	}

	public Map<String, String> checkoutPaymentMethod() {
		ILeafModular iModular = this.getPlanAttachement();
		
		PaymentCustomerModule customer = this.getPaymentCustomerModule();

		try {
			return this.stripeSubcriptionService.checkoutPaymentMethod(customer, iModular.getId());
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot perform plan checkout");
		}
	}

	public void setCustomerPaymentMethod(String iModularId, PaymentMethod pm) {
		ILeafModular planAttachment = this.getPlanAttachement(iModularId);
		PaymentCustomerModule customer = this.getPaymentCustomerModule(planAttachment);
		customer.setDefaultPaymentMethod(pm);
		customer.getMetadata().updateLastModification();
		
		this.savePlanAttachment(planAttachment);
	}
}
