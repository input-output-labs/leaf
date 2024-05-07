package fr.iolabs.leaf.payment.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.organization.OrganizationCreationEvent;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.PlanService;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@Component
public class PaymentCustomer_OrganizationCreationEventListener
		implements ApplicationListener<OrganizationCreationEvent> {
	@Autowired
	private PlanService planService;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Override
	public void onApplicationEvent(OrganizationCreationEvent event) {
		if (this.paymentConfig.getPlanAttachment() == PlanAttachment.ORGANIZATION) {
			LeafOrganization organization = event.getOrganization();
			PaymentCustomerModule customer = this.planService.getPaymentCustomerModule(organization);
			customer.setFreeTrialRemaining(this.paymentConfig.getDefaultFreeTrialRemaining());
		}
	}
}
