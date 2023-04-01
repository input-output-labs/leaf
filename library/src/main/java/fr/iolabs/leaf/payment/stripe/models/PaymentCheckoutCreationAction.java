package fr.iolabs.leaf.payment.stripe.models;

import java.util.List;
import java.util.Map;

public class PaymentCheckoutCreationAction {

	private List<StripeProduct> products;
	private String successUrl;
	private String cancelUrl;
	private StripePaymentModeEnum mode;
	private String customerId;
	private Map<String, Object> metadata;

	public List<StripeProduct> getProducts() {
		return products;
	}

	public void setProducts(List<StripeProduct> products) {
		this.products = products;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getCancelUrl() {
		return cancelUrl;
	}

	public void setCancelUrl(String cancelUrl) {
		this.cancelUrl = cancelUrl;
	}

	public StripePaymentModeEnum getMode() {
		return mode;
	}

	public void setMode(StripePaymentModeEnum mode) {
		this.mode = mode;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
}
