package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import com.stripe.model.Product;

import fr.iolabs.leaf.payment.stripe.models.PaymentLinkCreationAction;
import fr.iolabs.leaf.payment.stripe.models.StripeProduct;

@Service
public class StripeService {

	public Map<String, String> createPaymentLink(PaymentLinkCreationAction paymentLinkCreationAction) throws StripeException {
		List<Object> lineItems = this.createSoldItems(paymentLinkCreationAction.getProducts());

		Map<String, Object> params = new HashMap<>();
		params.put("line_items", lineItems);

		Map<String, Object> afterCompletionParams = this.createAfterCompletionParams(paymentLinkCreationAction.getRedirectUrlAfterPayment());
		params.put("after_completion", afterCompletionParams);
		
		// Create payment link: doc: https://stripe.com/docs/api/payment_links/payment_links/create?lang=java
		PaymentLink paymentLink = PaymentLink.create(params);
		Map<String, String> paymentLinkURL = new HashMap<>();
		paymentLinkURL.put("url", paymentLink.getUrl());
		return paymentLinkURL;
	}
	
	private List<Object> createSoldItems(List<StripeProduct> products) throws StripeException {
		List<Object> lineItems = new ArrayList<>();
		
		for(StripeProduct product: products) {
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
		// Creating the redirection url: format: { type: redirect, redirect: { url: "redirection.url" } }
		// Doc: https://stripe.com/docs/api/payment_links/payment_links/create?lang=java#create_payment_link-after_completion
		Map<String, Object> afterCompletionParams = new HashMap<>();
		afterCompletionParams.put("type", "redirect");
		Map<String, Object> redirect = new HashMap<>();
		redirect.put("url", redirectUrlAfterPayment);
		afterCompletionParams.put("redirect", redirect);
		return afterCompletionParams;
	}
}
