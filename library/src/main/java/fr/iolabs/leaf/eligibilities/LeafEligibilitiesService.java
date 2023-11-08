package fr.iolabs.leaf.eligibilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.organization.model.config.LeafOrganizationConfig;

@Service
public class LeafEligibilitiesService {
	@Autowired
	private LeafOrganizationConfig organizationConfig;
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
}
