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
import fr.iolabs.leaf.eligibilities.LeafEligibilitiesService;
import fr.iolabs.leaf.eligibilities.LeafEligibility;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Service
public class LeafPaymentPlanEligibilitiesComposer implements ApplicationListener<LeafEligibilitiesEvent> {

	@Resource(name = "coreContext")
	private LeafContext coreContext;
	
	@Autowired
	private PlanService planService;

	@Autowired
	private LeafEligibilitiesService eligibilitiesService;

	@Override
	public void onApplicationEvent(LeafEligibilitiesEvent event) {
		LeafPaymentPlan plan = this.planService.getSelectedOrDefaultPlan();
		if (plan == null) {
			return;
		}
		if (plan.isSuspended()) {
			plan = plan.getSuspensionBackupPlan();
		}
		this.getEligibilities(plan, event.eligibilities(), event.eligibilityKey());
	}

	public void getEligibilities(LeafPaymentPlan plan,
			Map<String, LeafEligibility> eligibilities, List<String> eligibilityKeys) {
		for (LeafPolicy policy : plan.getFeatures()) {
			if (eligibilityKeys == null || eligibilityKeys.contains(policy.getName())) {
				LeafEligibility existingEligibility = eligibilities.get(policy.getName());
				LeafEligibility eligibility = this.eligibilitiesService.readEligibility(policy, "Blocked by plan policies");
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
