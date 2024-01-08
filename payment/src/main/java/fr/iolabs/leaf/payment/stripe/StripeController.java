package fr.iolabs.leaf.payment.stripe;

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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("/api/payment/stripe")
public class StripeController {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;

	@Value("${leaf.payment.stripe.api.webhook-secret}")
	private String endpointSecret;

	@Autowired
	private StripeService stripeService;

	@Autowired
	private StripeHookService stripeHookService;

	private static Gson gson = new Gson();

	/**
	 * To uncomment for testing purposes
	 * 
	 * @CrossOrigin @PostMapping("/payment-links") public Map<String, String>
	 *              createPaymentLink(@RequestBody PaymentLinkCreationAction
	 *              paymentLinkCreationAction) throws StripeException {
	 *              Stripe.apiKey = this.privateKey; return
	 *              this.stripeService.createPaymentLink(paymentLinkCreationAction);
	 *              }
	 * 
	 * @CrossOrigin @PostMapping("/payment-intent") public String
	 *              createPaymentIntent(
	 * @RequestBody PaymentIntentCreationAction paymentIntentCreationAction) throws
	 *              StripeException { Stripe.apiKey = this.privateKey; return
	 *              gson.toJson(this.stripeService.createPaymentIntent(paymentIntentCreationAction));
	 *              }
	 * 
	 * @CrossOrigin @PostMapping("/payment-intent/capture") public String
	 *              capturePayment(
	 * @RequestBody PaymentIntentCaptureAction paymentIntentCaptureAction) throws
	 *              StripeException { Stripe.apiKey = this.privateKey; return
	 *              gson.toJson(this.stripeService.capturePayment(paymentIntentCaptureAction));
	 *              }
	 * 
	 *              /** To uncomment for testing purposes
	 * 
	 * @CrossOrigin @PostMapping("/payment-links") public Map<String, String>
	 *              createPaymentLink(@RequestBody PaymentLinkCreationAction
	 *              paymentLinkCreationAction) throws StripeException {
	 *              Stripe.apiKey = this.privateKey; return
	 *              this.stripeService.createPaymentLink(paymentLinkCreationAction);
	 *              }
	 * 
	 * @CrossOrigin @PostMapping("/checkout-sessions") public Map<String, Object>
	 *              createCheckoutSession(
	 * @RequestBody PaymentCheckoutCreationAction paymentCheckoutCreationAction)
	 *              throws StripeException { Stripe.apiKey = this.privateKey; return
	 *              this.stripeService.createCheckoutSession(paymentCheckoutCreationAction);
	 *              }
	 **/

	@PermitAll
	@CrossOrigin
	@PostMapping("/webhook")
	public ResponseEntity<Object> checkoutSessionWebhook(@RequestBody String eventReceived,
			@RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
		Event event = null;

		try {
			event = Webhook.constructEvent(eventReceived, sigHeader, this.endpointSecret);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			// Invalid payload
			return ResponseEntity.badRequest().build();
		} catch (SignatureVerificationException e) {
			e.printStackTrace();
			// Invalid signature
			return ResponseEntity.badRequest().build();
		}

		switch (event.getType()) {
		case "checkout.session.completed":
			this.stripeHookService.handleCheckoutSessionCompleted(event);
			break;
		case "customer.subscription.updated":
			// Subscription updated : check if trial is over
			this.stripeHookService.handleSubscriptionUpdated(event);
			break;
		case "customer.subscription.deleted":
			// Subscription deleted : no payment method at end of trial
			this.stripeHookService.handleSubscriptionDeleted(event);
			break;
		case "customer.subscription.trial_will_end":
			// Subscription trial will end in 3 days
			this.stripeHookService.handleSubscriptionTrialEnding(event);
			break;
		}
		return ResponseEntity.ok().build();
	}

}
