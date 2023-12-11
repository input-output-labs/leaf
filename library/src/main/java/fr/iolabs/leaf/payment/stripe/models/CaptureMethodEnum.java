package fr.iolabs.leaf.payment.stripe.models;

/**
 * Enum to define the payment intent capture method, useful to hold a payment and proceed later ("empreinte bancaire")
 * Doc: https://stripe.com/docs/api/payment_intents/create?lang=java#create_payment_intent-capture_method
 */
public enum CaptureMethodEnum {
	automatic,
	automatic_async,
	manual
}
