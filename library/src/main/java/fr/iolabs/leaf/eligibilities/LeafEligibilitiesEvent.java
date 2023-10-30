package fr.iolabs.leaf.eligibilities;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEvent;

public class LeafEligibilitiesEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private Map<String, LeafEligibility> eligibilities;
	private List<String> eligibilityKeys;

	public LeafEligibilitiesEvent(Object source, Map<String, LeafEligibility> eligibilities) {
		this(source, eligibilities, null);
	}

	public LeafEligibilitiesEvent(Object source, Map<String, LeafEligibility> eligibilities, List<String> eligibilityKeys) {
		super(source);
		this.eligibilities = eligibilities;
		this.eligibilityKeys = eligibilityKeys;
	}

	public Map<String, LeafEligibility> eligibilities() {
		return this.eligibilities;
	}

	public List<String> eligibilityKey() {
		return this.eligibilityKeys;
	}
}