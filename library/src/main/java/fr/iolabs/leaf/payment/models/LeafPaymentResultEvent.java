package fr.iolabs.leaf.payment.models;

import org.springframework.context.ApplicationEvent;

public class LeafPaymentResultEvent extends ApplicationEvent {
	private static final long serialVersionUID = 3168105661185044955L;

	private LeafPaymentTransaction transaction;
	
	public LeafPaymentResultEvent(Object source, LeafPaymentTransaction transaction) {
		super(source);
		this.transaction = transaction;
	}

	public LeafPaymentTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(LeafPaymentTransaction transaction) {
		this.transaction = transaction;
	}

}
