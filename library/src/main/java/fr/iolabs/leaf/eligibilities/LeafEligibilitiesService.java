package fr.iolabs.leaf.eligibilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.common.LeafPolicy;

@Service
public class LeafEligibilitiesService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public Map<String, LeafEligibility> getEligibilities() {
		return this.getEligibilities(null);
	}

	public Map<String, LeafEligibility> getEligibilities(List<String> eligibilityKeys) {
		Map<String, LeafEligibility> eligibilities = new HashMap<>();
		this.applicationEventPublisher.publishEvent(new LeafEligibilitiesEvent(this, eligibilities, eligibilityKeys));
		return eligibilities;
	}

	public LeafEligibility getEligibility(String eligibilityKey) {
		Map<String, LeafEligibility> eligibilities = new HashMap<>();
		this.applicationEventPublisher.publishEvent(new LeafEligibilitiesEvent(this, eligibilities, List.of(eligibilityKey)));
		return eligibilities.get(eligibilityKey);
	}

	public LeafEligibility readEligibility(LeafPolicy policy, String defaultIneligibilityReason) {
		String type = policy.getType();
		String value = policy.getValue();

		boolean eligible = false;
		switch (type) {
		case "boolean":
			eligible = "true".equals(value);
			break;
		default:
			LeafEligibilityEvent event = new LeafEligibilityEvent(this, policy);
			this.applicationEventPublisher.publishEvent(event);
			Optional<LeafEligibility> eligibility = event.eligibility();
			if (eligibility != null && eligibility.isPresent()) {
				return eligibility.get();
			}
		}
		LeafEligibility eligibility = new LeafEligibility(eligible);
		if (!eligible) {
			eligibility.reasons.add(defaultIneligibilityReason);
		}
		return eligibility;
	}
}
