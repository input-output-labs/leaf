package fr.iolabs.leaf.payment.plan.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.notifications.YamlPropertySourceFactory;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Component
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:payment.yml", factory = YamlPropertySourceFactory.class)
public class LeafPaymentConfig {
	private PlanAttachment planAttachment;
	private List<LeafPaymentPlan> plans;

	public PlanAttachment getPlanAttachment() {
		return planAttachment;
	}

	public void setPlanAttachment(PlanAttachment planAttachment) {
		this.planAttachment = planAttachment;
	}

	public List<LeafPaymentPlan> getPlans() {
		return plans;
	}

	public void setPlans(List<LeafPaymentPlan> plans) {
		this.plans = plans;
	}

	public LeafPaymentPlan getDefaultPlan() {
		for (LeafPaymentPlan plan : this.plans) {
			if (plan.isDefaultPlan()) {
				return plan;
			}
		}
		return null;
	}
}
