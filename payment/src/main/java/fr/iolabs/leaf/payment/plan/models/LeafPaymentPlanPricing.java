package fr.iolabs.leaf.payment.plan.models;

public class LeafPaymentPlanPricing {
	private Double price;
	private String period;
	private boolean free;

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}
}
