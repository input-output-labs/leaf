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
import com.stripe.net.RequestOptions;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.TaxIdCollectionCreateParams;
import com.stripe.param.UsageRecordCreateOnSubscriptionItemParams;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
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

	public void createSubscription(ILeafModular iModular, PaymentModule paymentModule, LeafPaymentPlan newPlan, LeafPaymentPlan previousPlan)
			throws StripeException {

		this.customerService.checkStripeCustomer(paymentModule);
		
		String existingSubscriptionId = null;
		if (previousPlan != null && previousPlan.getStripeSubscriptionId() != null) {
			existingSubscriptionId = previousPlan.getStripeSubscriptionId();
		}
		
		Subscription stripeSubscription = null;
		if (existingSubscriptionId != null) {
			// Update
			stripeSubscription = updateSubscription(existingSubscriptionId, newPlan, previousPlan);
		}
		if (existingSubscriptionId == null) {
			// Create
			stripeSubscription = createSubscription(iModular.getId(), paymentModule.getStripeCustomerId(), newPlan, paymentModule.getFreeTrialRemaining() > 0);
		}

		// Subscription
		newPlan.setStripeSubscriptionId(stripeSubscription.getId());
		newPlan.setSuspended(false);
		paymentModule.decreaseFreeTrialRemaining();
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

	public Subscription updateSubscription(String stripeSubscriptionId, LeafPaymentPlan newPlan, LeafPaymentPlan previousPlan) throws StripeException {
		try {
			Subscription stripeSubscription = Subscription.retrieve(stripeSubscriptionId);
			if (stripeSubscription != null && !"canceled".equals(stripeSubscription.getStatus())) {
				List<SubscriptionUpdateParams.Item> items = new ArrayList<>();

		        // Remove the previous price if provided
		        if (previousPlan != null && !previousPlan.getPricing().isFree() && previousPlan.getStripePriceId() != null) {
		            items.add(
		                SubscriptionUpdateParams.Item.builder()
		                        .setPrice(previousPlan.getStripePriceId())
		                        .setDeleted(true)
		                        .build()
		            );
		        }

		        // Add the new price if provided
		        if (newPlan != null && !newPlan.getPricing().isFree() && newPlan.getStripePriceId() != null) {
		            items.add(
		                SubscriptionUpdateParams.Item.builder()
		                        .setPrice(newPlan.getStripePriceId())
		                        .build()
		            );
		        }

		        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
		                .addAllItem(items)
		                .build();
		        
		        return stripeSubscription.update(params);
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
		return null;
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

	public void updateCustomerBillingDetails(ILeafModular iModular, PaymentModule paymentModule,
			LeafAccountProfile profile) throws StripeException {
		Customer stripeCustomer = this.customerService.checkStripeCustomer(paymentModule);

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
		
		stripeCustomer = Customer.retrieve(paymentModule.getStripeCustomerId(), params, null);
		
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
