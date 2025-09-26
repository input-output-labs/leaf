package fr.iolabs.leaf.payment.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.organization.OrganizationProfileUpdateEvent;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;
import fr.iolabs.leaf.payment.stripe.StripeSubcriptionService;

@Component
public class PaymentCustomer_OrganizationCorporateProfileUpdateEventListener2
		implements ApplicationListener<OrganizationProfileUpdateEvent> {
	@Autowired
	private StripeSubcriptionService stripeSubcriptionService;

	@Autowired
	private LeafCustomerService customerService;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Override
	public void onApplicationEvent(OrganizationProfileUpdateEvent event) {
		if (this.paymentConfig.getPlanAttachment() == PlanAttachment.ORGANIZATION && this.paymentConfig.isCollectTaxId()) {
			LeafOrganization organization = event.getOrganization();
			PaymentModule paymentModule = this.customerService.getPaymentModule(organization);
			try {
				this.stripeSubcriptionService.updateCustomerBillingDetails(organization, paymentModule,
						organization.getProfile());
			} catch (StripeException e) {
				e.printStackTrace();
				throw new InternalServerErrorException("Cannot update customer billing details");
			}
		}
	}
}
