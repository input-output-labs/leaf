package fr.iolabs.leaf.payment.plan;

import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanInfo;

@RestController
@RequestMapping("/api/payment/plans")
public class PlanController {
	@Autowired
	private PlanService planService;

	@PermitAll
	@CrossOrigin
	@GetMapping
	public List<LeafPaymentPlan> fetchPlans() {
		return this.planService.fetchPlans();
	}

	@CrossOrigin
	@PostMapping("/selected")
	public LeafPaymentPlan selectPlan(@RequestBody() LeafPaymentPlan selectedPlan) {
		return this.planService.selectPlan(selectedPlan);
	}

	@CrossOrigin
	@GetMapping("/selected")
	public LeafPaymentPlanInfo getSelectedPlan() {
		return this.planService.getSelectedPlan();
	}

	@CrossOrigin
	@PostMapping("/paymentmethod")
	public Map<String, String> checkoutPaymentMethod() {
		return this.planService.checkoutPaymentMethod();
	}
}
