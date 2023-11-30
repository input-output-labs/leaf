package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;

import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.models.PaymentSubscriptionModule;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentSubscription;

@Service
public class StripeSubcriptionService {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;
	
	public void createSubscription(PaymentCustomerModule customer, PaymentSubscriptionModule subscription, String productCode, String stripePriceId) throws StripeException {
		Stripe.apiKey = this.privateKey;
		
		this.checkStripeCustomer(customer);
		
		// Subscription
		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put(
		  "price",
		  stripePriceId
		);
		items.add(item1);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customer.getStripeId());
		params.put("items", items);

		Subscription stripeSubscription = Subscription.create(params);
		LeafPaymentSubscription leafPaymentSubscription = new LeafPaymentSubscription(stripeSubscription.getId(), productCode);
		subscription.addSubscription(leafPaymentSubscription);
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
			String name) {
		// TODO Implement this method
	}
}
