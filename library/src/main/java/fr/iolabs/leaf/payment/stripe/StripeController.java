package fr.iolabs.leaf.payment.stripe;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;

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

}
