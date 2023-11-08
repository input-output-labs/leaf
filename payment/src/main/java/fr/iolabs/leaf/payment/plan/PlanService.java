package fr.iolabs.leaf.payment.plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Service
public class PlanService {
	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public List<LeafPaymentPlan> fetchPlans() {
		return this.paymentConfig.getPlans();
	}
}
