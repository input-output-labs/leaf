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
import com.stripe.model.UsageRecord;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.UsageRecordCreateOnSubscriptionItemParams;

import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Service
public class StripeSubcriptionService implements InitializingBean {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;

	@Value("${leaf.protocol_hostname}")
	String protocol_hostname;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Override
	public void afterPropertiesSet() {
		Stripe.apiKey = this.privateKey;
	}

	public void createSubscription(ILeafModular iModular, PaymentCustomerModule customer, LeafPaymentPlan plan)
			throws StripeException {

		this.checkStripeCustomer(customer);

		// Subscription
		Subscription stripeSubscription = createSubscription(iModular.getId(), customer.getStripeId(), plan);
		plan.setStripeSubscriptionId(stripeSubscription.getId());
		plan.setSuspended(false);
	}

	private Subscription createSubscription(String innerId, String stripeCustomerId, LeafPaymentPlan plan)
			throws StripeException {

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("innerId", innerId);

		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", plan.getStripePriceId());
		items.add(item1);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", stripeCustomerId);
		params.put("trial_period_days", plan.getTrialDuration());
		params.put("items", items);
		params.put("metadata", metadata);

		return Subscription.create(params);
	}

	private void checkStripeCustomer(PaymentCustomerModule customer) throws StripeException {
		// Customer
		if (customer.getStripeId() != null) {
			// if here : verify it
			Customer.retrieve(customer.getStripeId());
		} else {
			// if missing, create it
			Map<String, Object> params = new HashMap<>();
			Customer stripeCustomer = Customer.create(params);
			customer.setStripeId(stripeCustomer.getId());
		}
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

	public Map<String, String> checkoutPaymentMethod(PaymentCustomerModule customer, String iModularId)
			throws StripeException {
		// Verify customer
		Customer.retrieve(customer.getStripeId());

		// Following instruction from:
		// https://stripe.com/docs/payments/checkout/subscriptions/update-payment-details#retrieve-checkout-session

		// Create checkout session
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("goal", "setPlanSubscriptionPaymentMethod");
		metadata.put("innerId", iModularId);
		metadata.put("customer_id", customer.getStripeId());
		Map<String, Object> setup_intent_data = new HashMap<>();
		setup_intent_data.put("metadata", metadata);

		Map<String, Object> params = new HashMap<>();
		params.put("success_url", this.protocol_hostname + paymentConfig.getRedirect().get("checkout_success"));
		params.put("cancel_url", this.protocol_hostname + paymentConfig.getRedirect().get("checkout_cancel"));
		params.put("payment_method_types", List.of("card"));

		params.put("mode", "setup");
		params.put("setup_intent_data", setup_intent_data);

		params.put("customer", customer.getStripeId());

		Session session = Session.create(params);

		Map<String, String> checkoutData = new HashMap<>();
		checkoutData.put("checkout_url", session.getUrl());
		return checkoutData;
	}

	public void sendUsageMetrics(String stripeSubscriptionId, long quantity) throws StripeException {
		UsageRecord.createOnSubscriptionItem(stripeSubscriptionId, UsageRecordCreateOnSubscriptionItemParams.builder()
				.setQuantity(quantity).setTimestamp(System.currentTimeMillis() / 1000).build(),
				RequestOptions.getDefault());
	}
}
