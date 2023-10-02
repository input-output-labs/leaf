package fr.iolabs.leaf.organization.policies;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.organization.model.OrganizationPolicies;
import fr.iolabs.leaf.organization.model.OrganizationPolicy;
import fr.iolabs.leaf.organization.model.OrganizationRole;
import fr.iolabs.leaf.organization.model.config.LeafDefaultRoleConfig;
import fr.iolabs.leaf.organization.model.config.LeafOrganizationConfig;

@Service
public class LeafOrganizationPoliciesService {
	@Autowired
	private LeafOrganizationConfig organizationConfig;

	public OrganizationPolicies createDefaultPolicies() {
		OrganizationPolicies defaultPolicies = new OrganizationPolicies();
		
		Map<String, OrganizationPolicy> configPolicies = organizationConfig.getPolicies();
		for(Map.Entry<String, OrganizationPolicy> configPolicy: configPolicies.entrySet()) {
			configPolicy.getValue().setName(configPolicy.getKey());
			defaultPolicies.getPolicies().add(configPolicy.getValue());
		}
		
		Map<String, LeafDefaultRoleConfig> configRoles = organizationConfig.getDefaultRoles();
		for(Map.Entry<String, LeafDefaultRoleConfig> configRole: configRoles.entrySet()) {
			OrganizationRole role = new OrganizationRole();
			role.setName(configRole.getKey());
			role.setCreatorDefault(configRole.getValue().isCreatorDefault());
			role.setOtherDefault(configRole.getValue().isOtherDefault());
			
			List<OrganizationPolicy> rights = defaultPolicies.copyPolicies();
			for (Map.Entry<String, String> policyDefaultValue: configRole.getValue().getPolicies().entrySet()) {
				for(OrganizationPolicy right : rights) {
					if (right.getName().equals(policyDefaultValue.getKey())) {
						right.setValue(policyDefaultValue.getValue());
					}
				}
			}
			role.setRights(rights);
			defaultPolicies.getRoles().add(role);
		}
		
		return defaultPolicies;
	}

	public String extractCreatorDefaultRole(OrganizationPolicies organizationPolicies) {
		for(OrganizationRole role : organizationPolicies.getRoles()) {
			if (role.isCreatorDefault()) {
				return role.getName();
			}
		}
		return null;
	}

	public String extractOtherDefaultRole(OrganizationPolicies organizationPolicies) {
		for(OrganizationRole role : organizationPolicies.getRoles()) {
			if (role.isOtherDefault()) {
				return role.getName();
			}
		}
		return null;
	}
}