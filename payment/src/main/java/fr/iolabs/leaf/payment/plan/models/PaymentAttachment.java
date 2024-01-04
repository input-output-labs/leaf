package fr.iolabs.leaf.payment.plan.models;

import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

public class PaymentAttachment {
	private PlanAttachment type;
	private String id;
	
	public PlanAttachment getType() {
		return type;
	}
	public void setType(PlanAttachment type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
