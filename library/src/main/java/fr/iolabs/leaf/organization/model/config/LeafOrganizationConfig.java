package fr.iolabs.leaf.organization.model.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.notifications.YamlPropertySourceFactory;
import fr.iolabs.leaf.organization.model.OrganizationPolicy;

@Component
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:organizations.yml", factory = YamlPropertySourceFactory.class)
public class LeafOrganizationConfig {
	private Map<String, OrganizationPolicy> policies;
	private Map<String, LeafDefaultRoleConfig> defaultRoles;

	public Map<String, OrganizationPolicy> getPolicies() {
		return policies;
	}

	public void setPolicies(Map<String, OrganizationPolicy> policies) {
		this.policies = policies;
	}

	public Map<String, LeafDefaultRoleConfig> getDefaultRoles() {
		return defaultRoles;
	}

	public void setDefaultRoles(Map<String, LeafDefaultRoleConfig> defaultRoles) {
		this.defaultRoles = defaultRoles;
	}
	
	public String getCreatorDefaultName() {
		for (Map.Entry<String, LeafDefaultRoleConfig > role: this.defaultRoles.entrySet()) {
			if (role.getValue().isCreatorDefault()) {
				return role.getKey();
			}
		}
		return null;
	}
	
	public String getOtherDefaultName() {
		for (Map.Entry<String, LeafDefaultRoleConfig > role: this.defaultRoles.entrySet()) {
			if (role.getValue().isOtherDefault()) {
				return role.getKey();
			}
		}
		return null;
	}
}
