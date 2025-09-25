package fr.iolabs.leaf.payment.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.PaymentModule.ExtraServicePrice;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.stripe.StripeSubcriptionService;

@Service
public class LeafServiceSubscriptionSynchronization {
	
	public static class LeafServiceSubscriptionSynchronizationAction {
		public String period;
		public List<ExtraServicePrice> extraServicePriceToAdd = new ArrayList();
		public List<ExtraServicePrice> extraServicePriceToUpdate = new ArrayList();
		public List<ExtraServicePrice> extraServicePriceToRemove = new ArrayList();
	}

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafServiceRepository leafServiceRepository;

	@Autowired
	private StripeSubcriptionService stripeSubcriptionService;

	@Autowired
	private LeafModuleService leafModuleService;
	@Async
	public void synchronizeServicesFor(PlanAttachment attachmentType, String attachedTo) {
		System.out.println("[LeafServiceSubscriptionSynchronization] synchronizeServicesFor called !");
		ILeafModular entity = this.getAttachement(attachmentType, attachedTo);
		if (entity == null) {
			return;
		}
		PaymentModule paymentModule = this.leafModuleService.get(PaymentModule.class, entity);
		List<LeafService> services = this.leafServiceRepository.findByAttachmentTypeAndAttachedTo(attachmentType, attachedTo).stream().filter(service -> service.isUseSubscription()).toList();
		LeafPaymentPlan selectedPlan = paymentModule.getSelectedPlan();
		if (selectedPlan == null) {
			System.out.println("/!\\ Condition not met for synchronization : no plan or no plan pricing");
			return;
		}
		if (selectedPlan.getPricing() == null) {
			System.out.println("/!\\ Condition not met for synchronization : no plan pricing");
			return;
		}
		if (selectedPlan.getPricing().getPeriod() == null) {
			System.out.println("/!\\ Condition not met for synchronization : no plan period");
			return;
		}
		if (selectedPlan.getStripeSubscriptionId() == null) {
			System.out.println("/!\\ Condition not met for synchronization : no stripe subscription id");
			return;
		}
		LeafServiceSubscriptionSynchronizationAction action = new LeafServiceSubscriptionSynchronizationAction();
		action.period = selectedPlan.getPricing().getPeriod();
		
		this.makeServicesDiff(paymentModule, services, action);
		
		this.stripeSubcriptionService.applyServiceDiffOnSubscription(paymentModule, action, selectedPlan.getStripeSubscriptionId());
		this.saveAttachment(entity);
	}

	private void makeServicesDiff(PaymentModule paymentModule, List<LeafService> services,
			LeafServiceSubscriptionSynchronizationAction action) {
		for (LeafService service : services) {
			boolean notFound = true;
			boolean differentPrice = false;
			for (ExtraServicePrice existingExtraServicePrice : paymentModule.getExtraServicePrices()) {
				if (action.period.equals(existingExtraServicePrice.period)) {
					if (service.getKey().equals(existingExtraServicePrice.service.getKey())) {
						notFound = false;
						if (service.getUnitPrice() != existingExtraServicePrice.service.getUnitPrice()) {
							differentPrice = true;
						}
					}
				}
			}
			if (notFound || differentPrice) {
				ExtraServicePrice toAdd = new ExtraServicePrice();
				toAdd.period = action.period;
				toAdd.service = service;
				action.extraServicePriceToAdd.add(toAdd);
			}
		}
		
		for (ExtraServicePrice existingExtraServicePrice : paymentModule.getExtraServicePrices()) {
			boolean differentPaymentPeriod = !action.period.equals(existingExtraServicePrice.period);
			boolean differentQuantity = false;
			boolean differentPrice = false;
			LeafService foundService = null;
			for (LeafService service : services) {
				if (service.getKey().equals(existingExtraServicePrice.service.getKey())) {
					foundService = service;
					
					if (service.getUnitPrice() != existingExtraServicePrice.service.getUnitPrice()) {
						differentPrice = true;
					}

					if (!service.isAutomaticQuantities() && service.getQuantity() != existingExtraServicePrice.service.getQuantity()) {
						differentQuantity = true;
					}
				}
			}
			if (differentPaymentPeriod || foundService == null || differentPrice) {
				action.extraServicePriceToRemove.add(existingExtraServicePrice);
			} else if (differentQuantity) {
				existingExtraServicePrice.service = foundService;
				action.extraServicePriceToUpdate.add(existingExtraServicePrice);
			}
		}
	}

	public ILeafModular getAttachement(PlanAttachment attachmentType, String attachedTo) {
		ILeafModular planOwnerTarget = null;
		switch (attachmentType) {
		case USER:
			Optional<LeafAccount> opt = this.accountRepository.findById(attachedTo);
			if (opt.isPresent()) {
				planOwnerTarget = opt.get();
			}
			break;
		case ORGANIZATION:
			Optional<LeafOrganization> optOrg = this.organizationRepository.findById(attachedTo);
			if (optOrg.isPresent()) {
				planOwnerTarget = optOrg.get();
			}
			break;
		default:
			break;
		}

		return planOwnerTarget;
	}

	private void saveAttachment(ILeafModular entity) {
		if (entity instanceof LeafAccount) {
			this.accountRepository.save((LeafAccount) entity);
		} else if (entity instanceof LeafOrganization) {
			this.organizationRepository.save((LeafOrganization) entity);
		}
	}
}
