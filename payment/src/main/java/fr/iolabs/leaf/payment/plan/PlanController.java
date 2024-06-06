package fr.iolabs.leaf.payment.plan;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanInfo;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlanTierSelection;

@RestController
@RequestMapping("/api/payment/plans")
public class PlanController {
	@Autowired
	private PlanService planService;
	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@PermitAll
	@CrossOrigin
	@GetMapping
	public List<LeafPaymentPlan> fetchPlans() {
		return this.planService.fetchPlans().stream().filter((plan) -> plan.isAvailable()).map(plan -> plan.clone()).collect(Collectors.toList());
	}

	@CrossOrigin
	@PostMapping("/selected")
	public LeafPaymentPlan selectPlan(@RequestBody() LeafPaymentPlan selectedPlan) {
		return this.planService.selectPlan(selectedPlan);
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/tier-selection")
	public LeafPaymentPlan selectPlanForTier(@RequestBody() LeafPaymentPlanTierSelection paymentPlanTierSelection) {
		Optional<LeafOrganization> optOrganization = organizationRepository.findById(paymentPlanTierSelection.getOrganizationId());
		if (optOrganization.isEmpty()) {
			throw new NotFoundException("No organization found with ID=" + paymentPlanTierSelection.getOrganizationId());
		}
		return this.planService.selectPlan(paymentPlanTierSelection.getPlanName(), optOrganization.get());
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
