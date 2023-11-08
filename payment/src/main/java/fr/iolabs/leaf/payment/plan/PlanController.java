package fr.iolabs.leaf.payment.plan;

import java.util.List;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

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
}
