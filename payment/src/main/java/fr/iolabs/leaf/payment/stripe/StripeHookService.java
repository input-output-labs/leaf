package fr.iolabs.leaf.payment.stripe;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Event.Data;
import com.stripe.model.SetupIntent;
import com.stripe.model.StripeObject;
import com.stripe.param.CustomerUpdateParams;

import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.LeafPaymentResultEvent;
import fr.iolabs.leaf.payment.models.LeafPaymentTransaction;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionRepository;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionStatusEnum;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.plan.PlanService;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@Service
public class StripeHookService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafPaymentTransactionRepository leafPaymentTransactionRepository;

	@Autowired
	private PlanService planService;

	@Autowired
	private LeafCustomerService customerService;

	public void handleCheckoutSessionCompleted(Event event) throws StripeException {
		// Deserialize the nested object inside the event
		Data eventData = event.getData();

		// String eventString = stripeObject.toJson();
		StripeObject checkoutSession = eventData.getObject();
		JsonObject jsonCheckoutSession = JsonParser.parseString(checkoutSession.toJson()).getAsJsonObject();
		
		String checkoutSessionId = jsonCheckoutSession.get("id").getAsString();
		String checkoutSessionMode = jsonCheckoutSession.get("mode").getAsString();

		// Extracting customer_details
		JsonObject customerDetails = jsonCheckoutSession.getAsJsonObject("customer_details");
		String customerEmail = customerDetails != null ? customerDetails.get("email").getAsString() : null;
		String customerName = customerDetails != null ? customerDetails.get("name").getAsString() : null;

		switch (checkoutSessionMode) {
		case "payment":
			this.handlePaymentCheckout(checkoutSessionId, customerEmail, customerName);
			break;
		case "setup":
			this.handleSetupCheckout(jsonCheckoutSession);
			break;
		}
	}

	private void handleSetupCheckout(JsonObject checkoutSession) throws StripeException {

		String setupIntentId = checkoutSession.get("setup_intent").getAsString();
		SetupIntent setupIntent = SetupIntent.retrieve(setupIntentId);

		switch (setupIntent.getMetadata().get("goal")) {
		case "setupCustomerDefaultPaymentMethod":
			String innerId = setupIntent.getMetadata().get("innerId");
			String innerType = setupIntent.getMetadata().get("innerType");
			String customerId = setupIntent.getCustomer();
			String paymentMethodId = setupIntent.getPaymentMethod();
			Customer customer = Customer.retrieve(customerId);

			CustomerUpdateParams params = CustomerUpdateParams.builder().setInvoiceSettings(
					CustomerUpdateParams.InvoiceSettings.builder().setDefaultPaymentMethod(paymentMethodId).build())
					.build();

			Customer updatedCustomer = customer.update(params);
			updatedCustomer.getInvoiceSettings()
					.getDefaultPaymentMethodObject();

			com.stripe.model.PaymentMethod stripePaymentMethod = com.stripe.model.PaymentMethod.retrieve(paymentMethodId);
			
			PaymentMethod pm = new PaymentMethod();
			pm.setBrand(stripePaymentMethod.getCard().getBrand());
			pm.setExpirationMonth("" + stripePaymentMethod.getCard().getExpMonth());
			pm.setExpirationYear("" + stripePaymentMethod.getCard().getExpYear());
			pm.setFunding(stripePaymentMethod.getCard().getFunding());
			pm.setLast4(stripePaymentMethod.getCard().getLast4());
			
			this.customerService.setCustomerPaymentMethod(innerType, innerId, pm);
			break;
		}
	}

	private void handlePaymentCheckout(String checkoutSessionId, String customerEmail, String customerName) {
		// Do we need to retrieve the payment intent to have a better payment
		// management? jsonObject.get("payment_intent")
		Optional<LeafPaymentTransaction> paymentTransactionOpt = this.leafPaymentTransactionRepository
				.findByCheckoutSessionId(checkoutSessionId.toString());

		if (paymentTransactionOpt.isPresent()) {
			LeafPaymentTransaction paymentTransaction = paymentTransactionOpt.get();
			paymentTransaction.setStatus(LeafPaymentTransactionStatusEnum.successful);
			if(customerEmail != null) {
				paymentTransaction.setCustomerEmail(customerEmail);
			}
			if(customerName != null) {
				paymentTransaction.setCustomerName(customerName);
			}
			this.leafPaymentTransactionRepository.save(paymentTransaction);
			this.applicationEventPublisher.publishEvent(new LeafPaymentResultEvent(this, paymentTransaction));
		} else {
			// TODO: Handle case where the transaction is not found.
		}
	}

	public void handleSubscriptionUpdated(Event event) {
		// Deserialize the nested object inside the event
		Data eventData = event.getData();
		StripeObject subscription = eventData.getObject();
		JsonObject jsonSubscription = JsonParser.parseString(subscription.toJson()).getAsJsonObject();
		String status = jsonSubscription.get("status").getAsString();
		
		if (!"trialing".equals(status)) {
			// Not in trial anymore
			JsonObject metadata = jsonSubscription.get("metadata").getAsJsonObject();
			String innerId = metadata.get("innerId").getAsString();
			
			this.planService.endPlanTrialFor(innerId);
		}
	}

	public void handleSubscriptionDeleted(Event event) {
		// Deserialize the nested object inside the event
		Data eventData = event.getData();
		StripeObject subscription = eventData.getObject();
		JsonObject jsonSubscription = JsonParser.parseString(subscription.toJson()).getAsJsonObject();
		String subscriptionId = jsonSubscription.get("id").getAsString();

		JsonObject metadata = jsonSubscription.get("metadata").getAsJsonObject();
		String innerId = metadata.get("innerId").getAsString();
		
		this.planService.stopPlanFor(innerId, subscriptionId);
	}

	public void handleSubscriptionTrialEnding(Event event) {
		// Deserialize the nested object inside the event
		Data eventData = event.getData();
		StripeObject subscription = eventData.getObject();
		JsonObject jsonSubscription = JsonParser.parseString(subscription.toJson()).getAsJsonObject();
		String subscriptionId = jsonSubscription.get("id").getAsString();

		JsonObject metadata = jsonSubscription.get("metadata").getAsJsonObject();
		String innerId = metadata.get("innerId").getAsString();
		
		this.planService.sendEndOfTrialApprochingFor(innerId, subscriptionId);
	}
	
}
