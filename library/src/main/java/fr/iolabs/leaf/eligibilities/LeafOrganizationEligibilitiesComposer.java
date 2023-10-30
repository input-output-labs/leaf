package fr.iolabs.leaf.eligibilities;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.organization.model.OrganizationPolicy;
import fr.iolabs.leaf.organization.model.OrganizationRole;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class LeafOrganizationEligibilitiesComposer implements ApplicationListener<LeafEligibilitiesEvent> {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Override
	public void onApplicationEvent(LeafEligibilitiesEvent event) {
		LeafAccount account = coreContext.getAccount();
		LeafOrganization organization = coreContext.getOrganization();
		if (account == null || organization == null) {
			return;
		}
		this.getEligibilities(account, organization, event.eligibilities(), event.eligibilityKey());
	}

	public void getEligibilities(LeafAccount account, LeafOrganization organization,
			Map<String, LeafEligibility> eligibilities, List<String> eligibilityKeys) {
		String userRole = "";
		for (OrganizationMembership member : organization.getMembers()) {
			if (account.getId().equals(member.getAccountId())) {
				userRole = member.getRole();
			}
		}
		for (OrganizationRole role : organization.getPolicies().getRoles()) {
			if (userRole.equals(role.getName())) {
				for (OrganizationPolicy policy : role.getRights()) {
					if (eligibilityKeys == null || eligibilityKeys.contains(policy.getName())) {
						LeafEligibility existingEligibility = eligibilities.get(policy.getName());
						LeafEligibility eligibility = this.readEligibility(policy);
						if (existingEligibility == null) {
							eligibilities.put(policy.getName(), eligibility);
						} else {
							LeafEligibility newEligibility = new LeafEligibility(
									existingEligibility.eligible && eligibility.eligible);
							newEligibility.reasons.addAll(existingEligibility.reasons);
							newEligibility.reasons.addAll(eligibility.reasons);
							eligibilities.put(policy.getName(), newEligibility);
						}
					}
				}
			}
		}
	}

	private LeafEligibility readEligibility(OrganizationPolicy policy) {
		String type = policy.getType();
		String value = policy.getValue();

		boolean eligible;
		switch (type) {
		case "boolean":
			eligible = "true".equals(value);
			break;
		default:
			// TODO: use event publisher to seek a subscriber to analyse the policy and
			// provide eligibility
			eligible = false;
		}
		LeafEligibility eligibility = new LeafEligibility(eligible);
		if (!eligible) {
			eligibility.reasons.add("Blocked by organization policies");
		}
		return eligibility;
	}
}