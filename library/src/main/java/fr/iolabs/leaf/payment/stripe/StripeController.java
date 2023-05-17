package fr.iolabs.leaf.payment.stripe;

import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;

import fr.iolabs.leaf.payment.stripe.models.PaymentCheckoutCreationAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentLinkCreationAction;

@RestController
@RequestMapping("/api/payment/stripe")
public class StripeController {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;
	
	@Value("${leaf.payment.stripe.api.webhook-secret}")
	private String endpointSecret;

	@Autowired
	private StripeService stripeService;
	
	/** To uncomment for testing purposes 
	@CrossOrigin
	@PostMapping("/payment-links")
	public Map<String, String> createPaymentLink(@RequestBody PaymentLinkCreationAction paymentLinkCreationAction)
			throws StripeException {
		Stripe.apiKey = this.privateKey;
		return this.stripeService.createPaymentLink(paymentLinkCreationAction);
	}
	**/

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
	public ResponseEntity<Object> checkoutSessionWebhook(@RequestBody String eventReceived, @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
        Event event = null;

        try {
          event = Webhook.constructEvent(eventReceived, sigHeader, this.endpointSecret);
        } catch (JsonSyntaxException e) {
          // Invalid payload
          return ResponseEntity.badRequest().build();
        } catch (SignatureVerificationException e) {
          // Invalid signature
          return ResponseEntity.badRequest().build();
        }

		event = ApiResource.GSON.fromJson(eventReceived, Event.class);
		if (event.getType().contains("checkout.session.completed")) {
			this.stripeService.handlePaymentResult(event);
		}
		return ResponseEntity.ok().build();
	}

}
