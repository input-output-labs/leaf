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
import fr.iolabs.leaf.payment.services.LeafService;

@Component
@Configuration
@ConfigurationProperties
@PropertySources({
    @PropertySource(value = "classpath:payment.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "classpath:payment-${spring.profiles.active}.yml", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class)
})
public class LeafPaymentConfig {
	private boolean collectTaxId;
	private PlanAttachment planAttachment;
	private List<LeafPaymentPlan> plans;
	private List<LeafService> services;
	private Map<String, String> redirect;
	private int defaultFreeTrialRemaining;

	public boolean isCollectTaxId() {
		return collectTaxId;
	}

	public void setCollectTaxId(boolean collectTaxId) {
		this.collectTaxId = collectTaxId;
	}

	public PlanAttachment getPlanAttachment() {
		return planAttachment;
	}

	public void setPlanAttachment(PlanAttachment planAttachment) {
		this.planAttachment = planAttachment;
	}

	public List<LeafPaymentPlan> getPlans() {
		Map<String, LeafPaymentPlan> allParentsPlans = plans.stream().filter(plan -> plan.getIsParent()).collect(Collectors.toMap(p -> p.getName(), p -> p));
		List<LeafPaymentPlan> nonAbstractPlans = plans.stream().filter(p -> !p.getIsParent()).toList();
		return nonAbstractPlans.stream().map(p -> p.enrichedCloning(allParentsPlans.get(p.getParentName()))).toList();
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

	public int getDefaultFreeTrialRemaining() {
		return defaultFreeTrialRemaining;
	}

	public void setDefaultFreeTrialRemaining(int defaultFreeTrialRemaining) {
		this.defaultFreeTrialRemaining = defaultFreeTrialRemaining;
	}

	public List<LeafService> getServices() {
		return services;
	}

	public void setServices(List<LeafService> services) {
		this.services = services;
	}
}
