package fr.iolabs.leaf.payment.stripe.models;

import java.util.List;

public class PaymentLinkCreationAction {
	
	private List<StripeProduct> products;
	private String redirectUrlAfterPayment;
	
	public List<StripeProduct> getProducts() {
		return products;
	}
	public void setProducts(List<StripeProduct> products) {
		this.products = products;
	}
	public String getRedirectUrlAfterPayment() {
		return redirectUrlAfterPayment;
	}
	public void setRedirectUrlAfterPayment(String redirectUrlAfterPayment) {
		this.redirectUrlAfterPayment = redirectUrlAfterPayment;
	}
}
