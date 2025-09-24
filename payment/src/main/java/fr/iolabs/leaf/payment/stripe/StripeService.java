package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Customer;
import com.stripe.model.PaymentLink;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerListPaymentMethodsParams;
import com.stripe.param.RefundCreateParams;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.LeafPaymentResultEvent;
import fr.iolabs.leaf.payment.models.LeafPaymentTransaction;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionRepository;
import fr.iolabs.leaf.payment.models.LeafPaymentTransactionStatusEnum;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.stripe.models.PaymentCheckoutCreationAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentIntentCancelAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentIntentCaptureAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentIntentCreationAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentIntentData;
import fr.iolabs.leaf.payment.stripe.models.PaymentIntentRefundAction;
import fr.iolabs.leaf.payment.stripe.models.PaymentLinkCreationAction;
import fr.iolabs.leaf.payment.stripe.models.StripeProduct;

@Service
public class StripeService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafPaymentTransactionRepository leafPaymentTransactionRepository;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Autowired
	private LeafCustomerService customerService;

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

	public Map<String, Object> createCheckoutSession(PaymentCheckoutCreationAction paymentCheckoutCreationAction)
			throws StripeException {
		Map<String, Object> params = new HashMap<>();

		if (paymentCheckoutCreationAction.getProducts() != null) {
			List<Object> lineItems = this.createSoldItems(paymentCheckoutCreationAction.getProducts());
			params.put("line_items", lineItems);
		}

		if (paymentCheckoutCreationAction.getCustomerId() != null) {
			// TODO RETRIEVE customer ID?
			params.put("customer", paymentCheckoutCreationAction.getCustomerId());
		}
		if (paymentCheckoutCreationAction.isUseCurrentAccount()) {
			PaymentModule paymentModule = this.customerService.getMyPaymentModule();
			LeafAccount account = this.coreContext.getAccount();
			Customer stripeCustomer = this.customerService.checkStripeCustomer(paymentModule, account.getEmail());
			params.put("customer", stripeCustomer.getId());
		}

		if (paymentCheckoutCreationAction.getPaymentIntentData() != null) {
			params.put("payment_intent_data",
					this.createPaymentIntentData(paymentCheckoutCreationAction.getPaymentIntentData()));
		}

		if (paymentCheckoutCreationAction.getCustomText() != null) {
			params.put("custom_text", paymentCheckoutCreationAction.getCustomText());
		}
		
		if (paymentCheckoutCreationAction.isEmbededUi()) {
			params.put("ui_mode", "embedded");
			params.put("redirect_on_completion", "never");
		} else {
			params.put("success_url", paymentCheckoutCreationAction.getSuccessUrl());
		}

		params.put("mode", paymentCheckoutCreationAction.getMode());

		HashMap<String, Object> metadata = new HashMap<>();
		metadata.put("paymentMetadata", paymentCheckoutCreationAction.getMetadata());
		params.put("metadata", paymentCheckoutCreationAction.getMetadata());

		Map<String, Object> tax_id_collection = new HashMap<>();
		tax_id_collection.put("enabled", this.paymentConfig.isCollectTaxId());
		params.put("tax_id_collection", tax_id_collection);

		if (paymentCheckoutCreationAction.isAllowPromotionCodes()) {
			params.put("allow_promotion_codes", true);
		}

		// Create checkout session: doc:
		// https://stripe.com/docs/api/checkout/sessions/create?lang=java
		Session session = Session.create(params);
		// TODO: ADD TAXES AUTO
		LeafPaymentTransaction paymentTransaction = new LeafPaymentTransaction(session.getCustomer());
		paymentTransaction.setStatus(LeafPaymentTransactionStatusEnum.inProgress);
		paymentTransaction.setModules(paymentCheckoutCreationAction.getMetadata());
		paymentTransaction.setCustomerId(session.getCustomer());
		paymentTransaction.setCheckoutSessionId(session.getId());
		paymentTransaction.setMetadata(ResourceMetadata.create());
		LeafPaymentTransaction paymentTransactionCreated = this.leafPaymentTransactionRepository
				.save(paymentTransaction);

		Map<String, Object> paymentSession = new HashMap<>();
		paymentSession.put("url", session.getUrl());
		paymentSession.put("clientSecret", session.getClientSecret());
		paymentSession.put("transactionId", paymentTransactionCreated.getId());
		paymentSession.put("sessionId", session.getId());
		if (session.getPaymentIntent() != null) {
			paymentSession.put("paymentIntentId", session.getPaymentIntent());
		}
		return paymentSession;
	}

	public PaymentIntent createPaymentIntent(PaymentIntentCreationAction paymentCreationAction) throws StripeException {
		Map<String, Object> automaticPaymentMethods = new HashMap<>();
		if (paymentCreationAction.isAutomaticPaymentMethods() == false) {
			automaticPaymentMethods.put("enabled", false);
		} else {
			// default case (automatic payment methods not defined) + true case
			automaticPaymentMethods.put("enabled", true);
		}
		Map<String, Object> params = new HashMap<>();
		params.put("amount", paymentCreationAction.getAmount());
		params.put("currency", paymentCreationAction.getCurrency());
		params.put("automatic_payment_methods", automaticPaymentMethods);
		if (paymentCreationAction.getCaptureMethod() != null) {
			params.put("capture_method", paymentCreationAction.getCaptureMethod());
		}
		// Create a PaymentIntent with the order amount and currency
		PaymentIntent intent = PaymentIntent.create(params);

		// Send PaymentIntent details to client
		return intent;
	}

	public PaymentIntent capturePayment(PaymentIntentCaptureAction paymentIntentCaptureAction) throws StripeException {
		PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentCaptureAction.getIntentId());
		Map<String, Object> params = new HashMap<>();
		params.put("amount", paymentIntentCaptureAction.getAmount());

		PaymentIntent capturedPayment = paymentIntent.capture(params);
		return capturedPayment;
	}

	public Refund refundPayment(PaymentIntentRefundAction paymentIntentRefundAction) throws StripeException {
		PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentRefundAction.getIntentId());
		
		RefundCreateParams params = RefundCreateParams.builder()
	            .setPaymentIntent(paymentIntent.getId())
	            .setAmount(paymentIntentRefundAction.getRefundedAmount())
	            .build();

	    Refund refund = Refund.create(params);
	    
		return refund;
	}

	public PaymentIntent cancelPayment(PaymentIntentCancelAction paymentIntentCancelAction) throws StripeException {
		PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentCancelAction.getIntentId());
		
		PaymentIntent cancelledPaymentIntent = paymentIntent.cancel();
	    
		return cancelledPaymentIntent;
	}

	public String retrievePaymentIntentFromCheckoutSessionId(String checkoutSessionId) throws StripeException {
		Session retrievedSession = Session.retrieve(checkoutSessionId);
		return retrievedSession.getPaymentIntent();
	}

	public void handlePaymentResult(Event event) {
		StripeObject eventData = event.getDataObjectDeserializer().getObject().orElseThrow();
		String eventString = eventData.toJson();
		JsonObject jsonObject = JsonParser.parseString(eventString).getAsJsonObject();
		String checkoutSessionIdWithQuotes = jsonObject.get("id").toString();
		String checkoutSessionId = checkoutSessionIdWithQuotes.substring(1, checkoutSessionIdWithQuotes.length() - 1);

		// Do we need to retrieve the payment intent to have a better payment
		// management? jsonObject.get("payment_intent")
		if (checkoutSessionId instanceof String) {
			Optional<LeafPaymentTransaction> paymentTransactionOpt = this.leafPaymentTransactionRepository
					.findByCheckoutSessionId(checkoutSessionId.toString());

			if (paymentTransactionOpt.isPresent()) {
				LeafPaymentTransaction paymentTransaction = paymentTransactionOpt.get();
				paymentTransaction.setStatus(LeafPaymentTransactionStatusEnum.successful);
				this.leafPaymentTransactionRepository.save(paymentTransaction);
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

	private Map<String, Object> createPaymentIntentData(PaymentIntentData paymentIntentData) {
		Map<String, Object> paymentIntentMap = new HashMap<>();
		paymentIntentMap.put("capture_method", paymentIntentData.getCaptureMethod());
		return paymentIntentMap;
	}

	public List<PaymentMethod> fetchCustomerPaymentMethods(String customerId) throws StripeException {
		Customer customer = Customer.retrieve(customerId);

		CustomerListPaymentMethodsParams params = CustomerListPaymentMethodsParams.builder()
				.setType(CustomerListPaymentMethodsParams.Type.CARD).build();

		PaymentMethodCollection paymentMethods = customer.listPaymentMethods(params);

		return paymentMethods.getData().stream().map((paymentMethod) -> {
			PaymentMethod pm = new PaymentMethod();
			pm.setBrand(paymentMethod.getCard().getBrand());
			pm.setFunding(paymentMethod.getCard().getFunding());
			pm.setExpirationMonth("" + paymentMethod.getCard().getExpMonth());
			pm.setExpirationYear("" + paymentMethod.getCard().getExpYear());
			pm.setLast4(paymentMethod.getCard().getLast4());
			return pm;
		}).collect(Collectors.toList());
	}
}
