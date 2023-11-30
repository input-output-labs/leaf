package fr.iolabs.leaf.eligibilities;

import java.util.Optional;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.common.LeafPolicy;

public class LeafEligibilityEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private LeafPolicy policy;
	private Optional<LeafEligibility> eligibility;

	public LeafEligibilityEvent(Object source, LeafPolicy policy) {
		super(source);
		this.policy = policy;
		this.eligibility = Optional.empty();
	}

	public Optional<LeafEligibility> eligibility() {
		return eligibility;
	}

	public void setEligibility(LeafEligibility eligibility) {
		this.eligibility = Optional.ofNullable(eligibility);
	}

	public LeafPolicy getPolicy() {
		return policy;
	}
}