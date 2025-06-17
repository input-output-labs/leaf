package fr.iolabs.leaf.eligibilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.LeafPolicy;
import fr.iolabs.leaf.organization.model.LeafOrganization;

@Service
public class LeafEligibilitiesService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public Map<String, LeafEligibility> getEligibilities() {
		return this.getEligibilities(null);
	}

	public Map<String, LeafEligibility> getEligibilities(List<String> eligibilityKeys) {
		LeafOrganization organization = this.coreContext.getOrganization();
		LeafAccount account = this.coreContext.getAccount();
		return this.getEligibilities(organization, account, eligibilityKeys);
	}

	public Map<String, LeafEligibility> getEligibilities(LeafOrganization organization, LeafAccount account, List<String> eligibilityKeys) {
		Map<String, LeafEligibility> eligibilities = new HashMap<>();
		this.applicationEventPublisher.publishEvent(new LeafEligibilitiesEvent(this, eligibilities, account, organization, eligibilityKeys));
		return eligibilities;
	}

	public LeafEligibility getEligibility(String eligibilityKey) {
		LeafOrganization organization = this.coreContext.getOrganization();
		LeafAccount account = this.coreContext.getAccount();
		return this.getEligibility(organization, account, eligibilityKey);
	}

	public LeafEligibility getEligibility(LeafOrganization organization, LeafAccount account, String eligibilityKey) {
		Map<String, LeafEligibility> eligibilities = new HashMap<>();
		this.applicationEventPublisher.publishEvent(new LeafEligibilitiesEvent(this, eligibilities, account, organization, List.of(eligibilityKey)));
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
