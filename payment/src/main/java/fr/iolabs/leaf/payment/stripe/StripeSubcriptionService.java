package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.models.PaymentSubscriptionModule;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentSubscription;

@Service
public class StripeSubcriptionService implements InitializingBean {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;

	@Override
	public void afterPropertiesSet() {
		Stripe.apiKey = this.privateKey;
	}

	public void createSubscription(PaymentCustomerModule customer, PaymentSubscriptionModule subscription,
			LeafPaymentPlan plan) throws StripeException {

		this.checkStripeCustomer(customer);

		// Subscription
		LeafPaymentSubscription paymentSubscription = subscription.findSubscription(plan.getName());
		if (paymentSubscription != null) {
			Subscription newStripeSubscription = createSubscription(customer, plan);
			paymentSubscription.setStripeSubscriptionId(newStripeSubscription.getId());
			paymentSubscription.setActive(true);
		} else {
			// Create new subscription
			Subscription stripeSubscription = createSubscription(customer, plan);
			LeafPaymentSubscription leafPaymentSubscription = new LeafPaymentSubscription(stripeSubscription.getId(),
					plan.getName());
			leafPaymentSubscription.setActive(true);
			subscription.addSubscription(leafPaymentSubscription);
		}
	}

	private Subscription createSubscription(PaymentCustomerModule customer, LeafPaymentPlan plan)
			throws StripeException {
		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", plan.getStripePriceId());
		items.add(item1);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customer.getStripeId());
		params.put("items", items);

		Subscription stripeSubscription = Subscription.create(params);
		return stripeSubscription;
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

	public void revokeSubscription(PaymentCustomerModule customer, PaymentSubscriptionModule subscription,
			LeafPaymentPlan plan) throws StripeException {
		LeafPaymentSubscription paymentSubscription = subscription.findSubscription(plan.getName());
		if (paymentSubscription != null) {
			try {
				Subscription stripeSubscription = Subscription.retrieve(paymentSubscription.getStripeSubscriptionId());
				if (stripeSubscription != null && !"canceled".equals(stripeSubscription.getStatus())) {
					Map<String, Object> params = new HashMap<>();
					params.put("invoice_now", true);
					params.put("prorate", true);

					stripeSubscription.cancel(params);
					paymentSubscription.setActive(false);
				}
			} catch (StripeException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, String> checkoutPaymentMethod(PaymentCustomerModule customer, String iModularId) throws StripeException {
		// Verify customer
		Customer.retrieve(customer.getStripeId());

		// Following instruction from:
		// https://stripe.com/docs/payments/checkout/subscriptions/update-payment-details#retrieve-checkout-session

		// Create checkout session
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("goal", "setPlanSubscriptionPaymentMethod");
		metadata.put("innerId", iModularId);
		metadata.put("customer_id", customer.getStripeId());
		metadata.put("subscription_id", customer.getStripeId());
		Map<String, Object> setup_intent_data = new HashMap<>();
		setup_intent_data.put("metadata", metadata);

		Map<String, Object> params = new HashMap<>();
		params.put("success_url", "http://localhost:4200/leaf-labs/payment");
		params.put("payment_method_types", List.of("card"));

		params.put("mode", "setup");
		params.put("setup_intent_data", setup_intent_data);

		params.put("customer", customer.getStripeId());

		Session session = Session.create(params);

		Map<String, String> checkoutData = new HashMap<>();
		checkoutData.put("checkout_url", session.getUrl());
		return checkoutData;
	}
}
