package fr.iolabs.leaf.payment.stripe;

import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.ApiResource;

import fr.iolabs.leaf.payment.stripe.models.PaymentCheckoutCreationAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentLinkCreationAction;

@RestController
@RequestMapping("/api/payment/stripe")
public class StripeController {

	private String privateKey = "coucou";

	@Autowired
	private StripeService stripeService;

	@CrossOrigin
	@PostMapping("/paymentLink")
	public Map<String, String> createPaymentLink(@RequestBody PaymentLinkCreationAction paymentLinkCreationAction)
			throws StripeException {
		Stripe.apiKey = this.privateKey;
		return this.stripeService.createPaymentLink(paymentLinkCreationAction);
	}

	@CrossOrigin
	@PostMapping("/checkout-sessions")
	public Map<String, Object> createCheckoutSession(
			@RequestBody PaymentCheckoutCreationAction paymentCheckoutCreationAction) throws StripeException {
		Stripe.apiKey = this.privateKey;
		return this.stripeService.createCheckoutSession(paymentCheckoutCreationAction);
	}

	@PermitAll
	@CrossOrigin
	@PostMapping("/checkout-sessions/webhook")
	public String checkoutSessionWebhook(@RequestBody Event eventReceived) throws StripeException {
		// VERIFY CALLS COME FROM STRIPE: https://stripe.com/docs/payments/checkout/fulfill-orders#v%C3%A9rifier-que-les-%C3%A9v%C3%A9nements-proviennent-de-stripe
		System.out.println(eventReceived);
		this.stripeService.handlePaymentResult(eventReceived);
//		try {
//			Object event = new com.google.gson.Gson();
//			Event event = ApiResource.GSON.fromJson(eventReceived, Event.class);
//		} catch (Exception error) {
//			System.out.println("An error occurred while receiving checkout event, " + error.toString());
//		}
		return "";
	}

}
