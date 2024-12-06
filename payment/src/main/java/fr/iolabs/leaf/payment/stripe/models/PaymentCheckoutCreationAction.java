package fr.iolabs.leaf.payment.stripe.models;

import java.util.List;
import java.util.Map;

public class PaymentCheckoutCreationAction {

	private List<StripeProduct> products;
	private String successUrl;
	private String cancelUrl;
	private PaymentIntentData paymentIntentData;
	private String customText;
	private StripePaymentModeEnum mode;
	private String customerId;
	private boolean useCurrentAccount;
	private boolean allowPromotionCodes;
	private boolean embededUi;
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

	public boolean isUseCurrentAccount() {
		return useCurrentAccount;
	}

	public void setUseCurrentAccount(boolean useCurrentAccount) {
		this.useCurrentAccount = useCurrentAccount;
	}

	public boolean isAllowPromotionCodes() {
		return allowPromotionCodes;
	}

	public void setAllowPromotionCodes(boolean allowPromotionCodes) {
		this.allowPromotionCodes = allowPromotionCodes;
	}

	public PaymentIntentData getPaymentIntentData() {
		return paymentIntentData;
	}

	public void setPaymentIntentData(PaymentIntentData paymentIntentData) {
		this.paymentIntentData = paymentIntentData;
	}

	public String getCustomText() {
		return customText;
	}

	public void setCustomText(String customText) {
		this.customText = customText;
	}

	public boolean isEmbededUi() {
		return embededUi;
	}

	public void setEmbededUi(boolean embededUi) {
		this.embededUi = embededUi;
	}
}
