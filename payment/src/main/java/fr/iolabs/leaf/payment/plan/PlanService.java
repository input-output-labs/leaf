package fr.iolabs.leaf.payment.plan;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.LeafInvoice;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanFeature;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanInfo;
import fr.iolabs.leaf.payment.plan.models.SelectedPlanModule;
import fr.iolabs.leaf.payment.stripe.StripeInvoicesService;
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

	@Autowired
	private StripeInvoicesService stripeInvoicesService;

	@Autowired
	private LeafNotificationService notificationService;

	@Autowired
	private LeafCustomerService customerService;

	public List<LeafPaymentPlan> fetchPlans() {
		int freeTrialRemaining = 1;
		ILeafModular planOwnerTarget = this.getPlanAttachement();
		if (planOwnerTarget != null) {
			PaymentCustomerModule customer = this.customerService.getPaymentCustomerModule(planOwnerTarget);
			freeTrialRemaining = customer.getFreeTrialRemaining();
		}
		
		List<LeafPaymentPlan> plans = this.paymentConfig.getPlans();
		
		if (freeTrialRemaining <= 0) {
			for (LeafPaymentPlan plan : plans) {
				plan.setTrialDuration(0);
			}
		}

		return plans;
	}

	private LeafPaymentPlan fetchPlanByName(String name) {
		return this.fetchPlanByName(name, true);
	}

	private LeafPaymentPlan fetchPlanByName(String name, boolean onlyAvailable) {
		for (LeafPaymentPlan plan : this.fetchPlans()) {
			if (name != null && name.equals(plan.getName())) {
				if (!onlyAvailable || plan.isAvailable()) {
					return plan;
				}
			}
		}
		return null;
	}

	public LeafPaymentPlan selectPlan(LeafPaymentPlan selectedPlan) {
		ILeafModular iModular = this.getPlanAttachement();
		return this.selectPlan(selectedPlan, iModular);
	}

	public LeafPaymentPlan selectBackupPlan(ILeafModular iModular) {
		return this.selectPlan(this.paymentConfig.getDefaultPlan(), iModular);
	}

	public LeafPaymentPlan selectPlan(LeafPaymentPlan selectedPlan, ILeafModular iModular) {
		LeafPaymentPlan availableSelectedPlan = this.fetchPlanByName(selectedPlan.getName());
		if (availableSelectedPlan == null) {
			throw new BadRequestException("The given plan does not exist or is not available");
		}
		return this.selectPlanUnsafe(availableSelectedPlan, iModular);
	}

	public LeafPaymentPlan selectPlan(String planName, ILeafModular iModular) {
		LeafPaymentPlan availableSelectedPlan = this.fetchPlanByName(planName, false);
		if (availableSelectedPlan == null) {
			throw new BadRequestException("The given plan does not exist");
		}
		return this.selectPlanUnsafe(availableSelectedPlan, iModular);
	}

	@Transactional
	public LeafPaymentPlan selectPlanUnsafe(LeafPaymentPlan availableSelectedPlan, ILeafModular iModular) {
		availableSelectedPlan.setSuspensionBackupPlan(this.paymentConfig.getDefaultPlan());

		// Revoke previous plan
		LeafPaymentPlan previousPlan = this.getCurrentPlan(iModular);
		if (previousPlan != null && !previousPlan.getPricing().isFree() && !previousPlan.isSuspended()) {
			this.revokePaymentSubscription(previousPlan);
		}

		// Create next plan
		if (availableSelectedPlan.getTrialDuration() > 0) {
			availableSelectedPlan.setInTrial(true);
		}
		availableSelectedPlan.setStartedAt(ZonedDateTime.now());

		this.attachPaymentPlanTo(availableSelectedPlan, iModular);
		if (!availableSelectedPlan.getPricing().isFree()) {
			this.createPaymentSubscription(iModular, availableSelectedPlan);
		}

		// Save plan selection
		this.savePlanAttachment(iModular);

		// This will update user or organization depending on selected plan
		this.applicationEventPublisher
				.publishEvent(new LeafPlanSelectionEvent(PlanService.class, this.paymentConfig.getPlanAttachment(), iModular, availableSelectedPlan, previousPlan));

		return availableSelectedPlan;
	}

	private LeafPaymentPlan getCurrentPlan(ILeafModular iModular) {
		SelectedPlanModule selectedPlan = this.getSelectedPlanModule(iModular);
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

	public PaymentCustomerModule getPaymentCustomerModule() {
		ILeafModular planOwnerTarget = this.getPlanAttachement();
		return this.customerService.getPaymentCustomerModule(planOwnerTarget);
	}

	private void createPaymentSubscription(ILeafModular iModular, LeafPaymentPlan selectedPlan) {
		PaymentCustomerModule customer = this.getPaymentCustomerModule();
		try {
			this.stripeSubcriptionService.createSubscription(iModular, customer, selectedPlan);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot create plan subscription");
		}
	}

	private void revokePaymentSubscription(LeafPaymentPlan plan) {
		try {
			this.stripeSubcriptionService.revokeSubscription(plan);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot create plan subscription");
		}
	}

	public LeafPaymentPlanInfo getSelectedPlan() {
		LeafPaymentPlanInfo paymentPlanInfo = new LeafPaymentPlanInfo();

		LeafPaymentPlan selectedPlan = this.getSelectedOrDefaultPlan();
		PaymentCustomerModule paymentCustomer = this.getPaymentCustomerModule();

		if (selectedPlan != null) {
			paymentPlanInfo.setPlan(selectedPlan);
		}

		if (paymentCustomer != null) {
			paymentPlanInfo.setPaymentMethod(paymentCustomer.getDefaultPaymentMethod());
		}

		return paymentPlanInfo;
	}
	
	public LeafPaymentPlan updateSelectedPlanFeaturesById(String id, List<LeafPaymentPlanFeature> updatedFeatures) {
		ILeafModular iModular = this.getPlanAttachement(id);

		LeafPaymentPlan selectedPlan = this.getSelectedPlanModule(iModular).getSelectedPlan();
		
		for (LeafPaymentPlanFeature updatedFeature: updatedFeatures) {
			for (LeafPaymentPlanFeature existingFeature: selectedPlan.getFeatures()) {
				if (existingFeature.getName().equals(updatedFeature.getName())) {
					existingFeature.setValue(updatedFeature.getValue());
				}
			}
		}
		
		this.savePlanAttachment(iModular);

		return selectedPlan;
	}
	
	public LeafPaymentPlanInfo getSelectedPlanById(String id) {
		ILeafModular iModular = this.getPlanAttachement(id);
		LeafPaymentPlanInfo paymentPlanInfo = new LeafPaymentPlanInfo();

		LeafPaymentPlan selectedPlan = this.getSelectedPlanModule(iModular).getSelectedPlan();
		PaymentCustomerModule paymentCustomer = this.customerService.getPaymentCustomerModule(iModular);

		if (selectedPlan != null) {
			paymentPlanInfo.setPlan(selectedPlan);
		}

		if (paymentCustomer != null) {
			paymentPlanInfo.setPaymentMethod(paymentCustomer.getDefaultPaymentMethod());
		}

		return paymentPlanInfo;
	}

	public Map<String, String> checkoutPaymentMethod() {
		ILeafModular iModular = this.getPlanAttachement();

		PaymentCustomerModule customer = this.getPaymentCustomerModule();
		PlanAttachment planAttachment = this.paymentConfig.getPlanAttachment();
		try {
			return this.customerService.checkoutPaymentMethod(customer, iModular.getId(), planAttachment.getValue());
		} catch (StripeException e) {
			throw new InternalServerErrorException("Cannot perform plan checkout");
		}
	}

	public LeafPaymentPlan getSelectedOrDefaultPlan() {
		ILeafModular iModular = this.getPlanAttachement();
		LeafPaymentPlan selectedPlan = null;
		if (iModular != null) {
			selectedPlan = this.getSelectedPlanModule(iModular).getSelectedPlan();
		}
		if (selectedPlan == null) {
			selectedPlan = this.paymentConfig.getDefaultPlan();
		}
		return selectedPlan;
	}

	public List<LeafInvoice> fetchInvoices() {
		PaymentCustomerModule customer = this.getPaymentCustomerModule();
		try {
			return this.stripeInvoicesService.getCustomerInvoices(customer);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot retrieve plan invoices");
		}
	}

	public void endPlanTrialFor(String iModularId) {
		ILeafModular planAttachment = this.getPlanAttachement(iModularId);
		SelectedPlanModule selectedPlanModule = this.getSelectedPlanModule(planAttachment);
		selectedPlanModule.getSelectedPlan().setInTrial(false);

		this.savePlanAttachment(planAttachment);
	}

	public void stopPlanFor(String iModularId, String subscriptionId) {
		ILeafModular planAttachment = this.getPlanAttachement(iModularId);
		if (planAttachment == null) {
			throw new BadRequestException("No attachment with id: " + iModularId);
		}
		SelectedPlanModule selectedPlanModule = this.getSelectedPlanModule(planAttachment);

		LeafPaymentPlan selectedPlan = selectedPlanModule.getSelectedPlan();
		if (selectedPlan != null) {
			boolean hasCorrectSubscriptionId = selectedPlan.getStripeSubscriptionId() != null
					&& selectedPlan.getStripeSubscriptionId().equals(subscriptionId);
			if (hasCorrectSubscriptionId) {
				selectedPlanModule.getSelectedPlan().setSuspended(true);

				this.selectBackupPlan(planAttachment);

				String targetAccountId = this.getPlanAttachementNotificationTargetAccountId(planAttachment);
				if (targetAccountId != null) {
					this.notificationService.emit(LeafNotification.of("LEAF_PAYMENT_PLAN_DELETED", targetAccountId));
				}
			}
		}
	}

	public void sendEndOfTrialApprochingFor(String iModularId, String subscriptionId) {
		ILeafModular planAttachment = this.getPlanAttachement(iModularId);
		SelectedPlanModule selectedPlanModule = this.getSelectedPlanModule(planAttachment);
		PaymentCustomerModule paymentCustomerModule = this.customerService.getPaymentCustomerModule(planAttachment);

		LeafPaymentPlan selectedPlan = selectedPlanModule.getSelectedPlan();
		if (selectedPlan != null) {
			boolean isInTrial = selectedPlan.isInTrial();
			boolean isNotSuspended = !selectedPlan.isSuspended();
			boolean hasCorrectSubscriptionId = selectedPlan.getStripeSubscriptionId() != null
					&& selectedPlan.getStripeSubscriptionId().equals(subscriptionId);
			boolean defaultPaymentMethodDefined = paymentCustomerModule.getDefaultPaymentMethod() == null;
			if (isInTrial && isNotSuspended && hasCorrectSubscriptionId && defaultPaymentMethodDefined) {
				// if no payment method, send no payment method notification
				String targetAccountId = this.getPlanAttachementNotificationTargetAccountId(planAttachment);
				if (targetAccountId != null) {
					this.notificationService
							.emit(LeafNotification.of("LEAF_PAYMENT_PLAN_TRIAL_ENDING_SOON", targetAccountId));
				}
			}
		}
	}

	public void sendUsageMetrics(String iModularId, long quantity) {
		ILeafModular planAttachment = this.getPlanAttachement(iModularId);
		SelectedPlanModule selectedPlanModule = this.getSelectedPlanModule(planAttachment);

		LeafPaymentPlan selectedPlan = selectedPlanModule.getSelectedPlan();
		if (selectedPlan != null && !selectedPlan.isSuspended() && selectedPlan.getStripeSubscriptionId() != null
				&& selectedPlan.getStripePriceId() != null) {
			try {
				this.stripeSubcriptionService.sendUsageMetrics(selectedPlan.getStripeSubscriptionId(),
						selectedPlan.getStripePriceId(), quantity);
			} catch (StripeException e) {
				e.printStackTrace();
			}
		}
	}

	private String getPlanAttachementNotificationTargetAccountId(ILeafModular iModular) {
		if (iModular instanceof LeafAccount) {
			return iModular.getId();
		} else if (iModular instanceof LeafOrganization) {
			LeafOrganization organization = (LeafOrganization) iModular;
			OrganizationMembership firstMember = organization.getMembers().get(0);
			if (firstMember != null) {
				return firstMember.getAccountId();
			}
		}
		return null;
	}
}
