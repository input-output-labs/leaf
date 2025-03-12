package fr.iolabs.leaf.payment.plan.config;

public enum PlanAttachment {
	USER("USER"),
	ORGANIZATION("ORGANIZATION");
	
    private final String value;
    
    PlanAttachment(final String value) {
        this.value = value;
    }

	public String getValue() {
		return value;
	}
}
