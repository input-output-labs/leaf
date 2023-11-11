package fr.iolabs.leaf.payment.plan;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.LeafPolicy;
import fr.iolabs.leaf.eligibilities.LeafEligibilitiesEvent;
import fr.iolabs.leaf.eligibilities.LeafEligibility;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Service
public class LeafPaymentPlanEligibilitiesComposer implements ApplicationListener<LeafEligibilitiesEvent> {

	@Resource(name = "coreContext")
	private LeafContext coreContext;
	
	@Autowired
	private PlanService planService;

	@Override
	public void onApplicationEvent(LeafEligibilitiesEvent event) {
		LeafPaymentPlan plan = this.planService.getSelectedPlanModule().getSelectedPlan();
		if (plan == null) {
			return;
		}
		this.getEligibilities(plan, event.eligibilities(), event.eligibilityKey());
	}

	public void getEligibilities(LeafPaymentPlan plan,
			Map<String, LeafEligibility> eligibilities, List<String> eligibilityKeys) {
		for (LeafPolicy policy : plan.getFeatures()) {
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

	private LeafEligibility readEligibility(LeafPolicy policy) {
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
			eligibility.reasons.add("Blocked by plan policies");
		}
		return eligibility;
	}

}
