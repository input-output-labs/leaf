package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.TaxId;
import com.stripe.model.UsageRecord;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.TaxIdCollectionCreateParams;
import com.stripe.param.UsageRecordCreateOnSubscriptionItemParams;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Service
public class StripeSubcriptionService implements InitializingBean {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;

	@Autowired
	private LeafCustomerService customerService;
	
	@Override
	public void afterPropertiesSet() {
		Stripe.apiKey = this.privateKey;
	}

	public void createSubscription(ILeafModular iModular, PaymentCustomerModule customer, LeafPaymentPlan plan)
			throws StripeException {

		this.customerService.checkStripeCustomer(customer);
		
		int freeTrialRemaining = customer.getFreeTrialRemaining();

		// Subscription
		Subscription stripeSubscription = createSubscription(iModular.getId(), customer.getStripeId(), plan, freeTrialRemaining > 0);
		plan.setStripeSubscriptionId(stripeSubscription.getId());
		plan.setSuspended(false);
		customer.decreaseFreeTrialRemaining();
	}

	private Subscription createSubscription(String innerId, String stripeCustomerId, LeafPaymentPlan plan, boolean trialAllowed)
			throws StripeException {
		Map<String, Object> params = new HashMap<>();
		params.put("customer", stripeCustomerId);

		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", plan.getStripePriceId());
		items.add(item1);
		params.put("items", items);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("innerId", innerId);
		params.put("metadata", metadata);

		if (trialAllowed) {
			params.put("trial_period_days", plan.getTrialDuration());
			Map<String, Object> trial_settings = new HashMap<>();
			Map<String, Object> end_behavior = new HashMap<>();
			end_behavior.put("missing_payment_method", "cancel");
			trial_settings.put("end_behavior", end_behavior);
			params.put("trial_settings", trial_settings);
		}

		return Subscription.create(params);
	}

	public void revokeSubscription(LeafPaymentPlan plan) throws StripeException {
		try {
			Subscription stripeSubscription = Subscription.retrieve(plan.getStripeSubscriptionId());
			if (stripeSubscription != null && !"canceled".equals(stripeSubscription.getStatus())) {
				Map<String, Object> params = new HashMap<>();
				params.put("invoice_now", true);
				params.put("prorate", true);

				stripeSubscription.cancel(params);
				plan.setSuspended(true);
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
	}

	public void sendUsageMetrics(String stripeSubscriptionId, String stripePriceId, long quantity)
			throws StripeException {
		Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
		for (SubscriptionItem subscriptionItem : subscription.getItems().getData()) {
			if (subscriptionItem.getPrice().getId().equals(stripePriceId)) {
				UsageRecord.createOnSubscriptionItem(
						subscriptionItem.getId(), UsageRecordCreateOnSubscriptionItemParams.builder()
								.setQuantity(quantity).setTimestamp(System.currentTimeMillis() / 1000).build(),
						RequestOptions.getDefault());
				break;
			}
		}
	}

	public void updateCustomerBillingDetails(ILeafModular iModular, PaymentCustomerModule customer,
			LeafAccountProfile profile) throws StripeException {
		Customer stripeCustomer = this.customerService.checkStripeCustomer(customer);

		// Update billing address
		Map<String, Object> updateParams = new HashMap<>();
		if (profile.getAddress() != null) {
			Map<String, Object> address = new HashMap<>();
			if (profile.getAddress().getAddress() != null) {
				address.put("line1", profile.getAddress().getAddress());
			}
			if (profile.getAddress().getPostalCode() != null) {
				address.put("postal_code", profile.getAddress().getPostalCode());
			}
			if (profile.getAddress().getCity() != null) {
				address.put("city", profile.getAddress().getCity());
			}
			if (profile.getAddress().getCountry() != null) {
				address.put("country", profile.getAddress().getCountry());
			}
			updateParams.put("address", address);
		}
		String customerName = null;
		if (profile.getFirstname() != null && !profile.getFirstname().isBlank() && profile.getLastname() != null && !profile.getLastname().isBlank()) {
			customerName = profile.getFirstname() + " " + profile.getLastname();
		}
		if (customerName == null && profile.getUsername() != null) {
			customerName = profile.getUsername();
		}
		if (profile.isCorporate() && profile.getCompanyName() != null && !profile.getCompanyName().isBlank()) {
			customerName = profile.getCompanyName();
		}
		if (customerName != null) {
			updateParams.put("name", customerName);
		}
		if (updateParams.size() > 0) {
			stripeCustomer = stripeCustomer.update(updateParams);
		}
		

		List<String> expandList = new ArrayList<>();
		expandList.add("tax_ids");
		Map<String, Object> params = new HashMap<>();
		params.put("expand", expandList);
		
		stripeCustomer = Customer.retrieve(customer.getStripeId(), params, null);
		
		// Update tax id
		if (profile.isCorporate() && profile.getAddress() != null && profile.getAddress().getCountry() != null && profile.getTaxId() != null && !profile.getTaxId().isEmpty()) {
			TaxIdCollectionCreateParams.Type taxType = TaxIdCollectionCreateParams.Type.EU_VAT;
			switch (profile.getAddress().getCountry()) {
			case "CH":
				taxType = TaxIdCollectionCreateParams.Type.CH_VAT;
				break;
			case "FR":
			case "BE":
			case "LU":
			default:
				taxType = TaxIdCollectionCreateParams.Type.EU_VAT;
				break;
			}
			TaxIdCollectionCreateParams taxIdParams = TaxIdCollectionCreateParams.builder().setType(taxType)
					.setValue(profile.getTaxId()).build();
			TaxId existingTaxIdForType = null;
			for(TaxId tax : stripeCustomer.getTaxIds().getData()) {
				if (tax.getType().equals(taxType.getValue())) {
					existingTaxIdForType = tax;
					break;
				}
			}
			
			if (existingTaxIdForType != null) {
				if (!existingTaxIdForType.getValue().equals(profile.getTaxId())) {
					existingTaxIdForType.delete();
					stripeCustomer.getTaxIds().create(taxIdParams);
				}
			} else {
				stripeCustomer.getTaxIds().create(taxIdParams);
			}
		}
	}
}
