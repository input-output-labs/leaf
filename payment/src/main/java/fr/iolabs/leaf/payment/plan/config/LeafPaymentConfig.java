package fr.iolabs.leaf.payment.plan.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.notifications.YamlPropertySourceFactory;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;

@Component
@Configuration
@ConfigurationProperties
@PropertySources({
    @PropertySource(value = "classpath:payment.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "classpath:payment-${spring.profiles.active}.yml", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class)
})
public class LeafPaymentConfig {
	private PlanAttachment planAttachment;
	private List<LeafPaymentPlan> plans;
	private Map<String, String> redirect;

	public PlanAttachment getPlanAttachment() {
		return planAttachment;
	}

	public void setPlanAttachment(PlanAttachment planAttachment) {
		this.planAttachment = planAttachment;
	}

	public List<LeafPaymentPlan> getPlans() {
		return plans.stream().map(plan -> plan.clone()).collect(Collectors.toList());
	}

	public void setPlans(List<LeafPaymentPlan> plans) {
		this.plans = plans;
	}

	public LeafPaymentPlan getDefaultPlan() {
		for (LeafPaymentPlan plan : this.getPlans()) {
			if (plan.isDefaultPlan()) {
				return plan;
			}
		}
		return null;
	}

	public Map<String, String> getRedirect() {
		return redirect;
	}

	public void setRedirect(Map<String, String> redirect) {
		this.redirect = redirect;
	}
}
