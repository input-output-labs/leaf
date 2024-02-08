package fr.iolabs.leaf.payment.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.organization.OrganizationProfileUpdateEvent;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.PlanService;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.stripe.StripeSubcriptionService;

@Component
public class PaymentCustomer_OrganizationCorporateProfileUpdateEventListener
		implements ApplicationListener<OrganizationProfileUpdateEvent> {
	@Autowired
	private StripeSubcriptionService stripeSubcriptionService;

	@Autowired
	private PlanService planService;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Override
	public void onApplicationEvent(OrganizationProfileUpdateEvent event) {
		if (this.paymentConfig.isCollectTaxId()) {
			LeafOrganization organization = event.getOrganization();
			PaymentCustomerModule customer = this.planService.getPaymentCustomerModule(organization);
			try {
				if (organization.getProfile().getCorporate()) {
					this.stripeSubcriptionService.updateCustomerBillingDetails(organization, customer,
							organization.getProfile());
				}
			} catch (StripeException e) {
				e.printStackTrace();
				throw new InternalServerErrorException("Cannot update customer billing details");
			}
		}
	}
}
