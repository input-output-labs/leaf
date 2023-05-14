package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Event.Data;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.models.LeafPaymentResultEvent;
import fr.iolabs.leaf.payment.models.LeafPaymentTransaction;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionRepository;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionStatusEnum;
import fr.iolabs.leaf.payment.stripe.models.PaymentCheckoutCreationAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentLinkCreationAction;
import fr.iolabs.leaf.payment.stripe.models.StripeProduct;

@Service
public class StripeService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private LeafPaymentTransactionRepository leafPaymentTransactionRepository;
	
	public Map<String, String> createPaymentLink(PaymentLinkCreationAction paymentLinkCreationAction)
			throws StripeException {
		List<Object> lineItems = this.createSoldItems(paymentLinkCreationAction.getProducts());

		Map<String, Object> params = new HashMap<>();
		params.put("line_items", lineItems);

		Map<String, Object> afterCompletionParams = this
				.createAfterCompletionParams(paymentLinkCreationAction.getRedirectUrlAfterPayment());
		params.put("after_completion", afterCompletionParams);

		// Create payment link: doc:
		// https://stripe.com/docs/api/payment_links/payment_links/create?lang=java
		PaymentLink paymentLink = PaymentLink.create(params);
		Map<String, String> paymentLinkURL = new HashMap<>();

		paymentLinkURL.put("url", paymentLink.getUrl());
		return paymentLinkURL;
	}

	public Map<String, Object> createCheckoutSession(PaymentCheckoutCreationAction paymentCheckoutCreationAction) throws StripeException {
		List<Object> lineItems = this.createSoldItems(paymentCheckoutCreationAction.getProducts());
		
		Map<String, Object> params = new HashMap<>();
		params.put("line_items", lineItems);
		
		if(paymentCheckoutCreationAction.getCustomerId() != null) {
			//TODO RETRIEVE customer ID?
			params.put("customer", paymentCheckoutCreationAction.getCustomerId());
		}
		
		params.put("success_url", paymentCheckoutCreationAction.getSuccessUrl());
		params.put("mode", paymentCheckoutCreationAction.getMode());
		
		HashMap<String, Object> metadata = new HashMap<>();
		metadata.put("paymentMetadata", paymentCheckoutCreationAction.getMetadata());
		params.put("metadata", paymentCheckoutCreationAction.getMetadata());

		// Create checkout session: doc:
		// https://stripe.com/docs/api/checkout/sessions/create?lang=java
		Session session = Session.create(params);
		//TODO: ADD TAXES AUTO
		LeafPaymentTransaction paymentTransaction = new LeafPaymentTransaction(session.getCustomer());
		paymentTransaction.setStatus(LeafPaymentTransactionStatusEnum.inProgress);
		paymentTransaction.setModules(paymentCheckoutCreationAction.getMetadata());
		paymentTransaction.setCustomerId(session.getCustomer());
		paymentTransaction.setCheckoutSessionId(session.getId());
		paymentTransaction.setMetadata(ResourceMetadata.create());
		LeafPaymentTransaction paymentTransactionCreated = this.leafPaymentTransactionRepository.save(paymentTransaction);
		
		Map<String, Object> paymentSession = new HashMap<>();
		paymentSession.put("url", session.getUrl());
		paymentSession.put("transactionId", paymentTransactionCreated.getId());
		return paymentSession;
	}
	
	public void handlePaymentResult(Event event) {
		StripeObject eventData =  event.getDataObjectDeserializer().getObject().orElseThrow();
		String eventString = eventData.toJson();
		JsonObject jsonObject = JsonParser.parseString(eventString).getAsJsonObject();
		String checkoutSessionIdWithQuotes = jsonObject.get("id").toString();
		String checkoutSessionId = checkoutSessionIdWithQuotes.substring(1, checkoutSessionIdWithQuotes.length() - 1);

		// Do we need to retrieve the payment intent to have a better payment management? jsonObject.get("payment_intent")
		if(checkoutSessionId instanceof String) {
			Optional<LeafPaymentTransaction> paymentTransactionOpt = this.leafPaymentTransactionRepository.findByCheckoutSessionId(checkoutSessionId.toString());
			
			if(paymentTransactionOpt.isPresent()) {
				LeafPaymentTransaction paymentTransaction = paymentTransactionOpt.get();
				paymentTransaction.setStatus(LeafPaymentTransactionStatusEnum.successful);
				this.leafPaymentTransactionRepository.save(paymentTransaction);
				System.out.println("Yes status before publish " + paymentTransaction.getStatus());
				this.applicationEventPublisher.publishEvent(new LeafPaymentResultEvent(this, paymentTransaction));
			} else {
				// TODO: Handle case where the transaction is not found.
			}
		}
	}

	private List<Object> createSoldItems(List<StripeProduct> products) throws StripeException {
		List<Object> lineItems = new ArrayList<>();

		for (StripeProduct product : products) {
			// Create product: doc: https://stripe.com/docs/api/products/object?lang=java
			Map<String, Object> productParams = new HashMap<>();
			productParams.put("name", product.getProductName());
			Product createdProduct = Product.create(productParams);

			// Create price: doc: https://stripe.com/docs/api/prices/create?lang=java
			Map<String, Object> priceParams = new HashMap<>();
			priceParams.put("unit_amount", product.getPrice());
			priceParams.put("currency", product.getCurrency());
			priceParams.put("product", createdProduct.getId());
			Price price = Price.create(priceParams);

			Map<String, Object> lineItem = new HashMap<>();
			lineItem.put("price", price.getId());
			lineItem.put("quantity", product.getQuantity());
			lineItems.add(lineItem);
		}
		return lineItems;

	}

	private Map<String, Object> createAfterCompletionParams(String redirectUrlAfterPayment) {
		// Creating the redirection url: format: { type: redirect, redirect: { url:
		// "redirection.url" } }
		// Doc:
		// https://stripe.com/docs/api/payment_links/payment_links/create?lang=java#create_payment_link-after_completion
		Map<String, Object> afterCompletionParams = new HashMap<>();
		afterCompletionParams.put("type", "redirect");
		Map<String, Object> redirect = new HashMap<>();
		redirect.put("url", redirectUrlAfterPayment);
		afterCompletionParams.put("redirect", redirect);
		return afterCompletionParams;
	}
}
