package fr.iolabs.leaf.organization.policies;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.LeafOrganizationAuthorizationsService;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationPolicies;
import fr.iolabs.leaf.organization.model.OrganizationPolicy;
import fr.iolabs.leaf.organization.model.OrganizationRole;
import fr.iolabs.leaf.organization.model.config.LeafDefaultRoleConfig;
import fr.iolabs.leaf.organization.model.config.LeafOrganizationConfig;

@Service
public class LeafOrganizationPoliciesService {
	@Autowired
	private LeafOrganizationConfig organizationConfig;

	@Autowired
	private LeafOrganizationAuthorizationsService organizationAuthorizationsService;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	public OrganizationPolicies createDefaultPolicies() {
		OrganizationPolicies defaultPolicies = this.extractDefaultPolicies();

		Map<String, LeafDefaultRoleConfig> configRoles = organizationConfig.getDefaultRoles();
		for (Map.Entry<String, LeafDefaultRoleConfig> configRole : configRoles.entrySet()) {
			OrganizationRole role = createRole(defaultPolicies, configRole.getKey(), configRole.getValue());
			defaultPolicies.getRoles().add(role);
		}

		return defaultPolicies;
	}

	private OrganizationPolicies extractDefaultPolicies() {
		OrganizationPolicies defaultPolicies = new OrganizationPolicies();
		Map<String, OrganizationPolicy> configPolicies = organizationConfig.getPolicies();
		for (Map.Entry<String, OrganizationPolicy> configPolicy : configPolicies.entrySet()) {
			configPolicy.getValue().setName(configPolicy.getKey());
			defaultPolicies.getPolicies().add(configPolicy.getValue());
		}
		return defaultPolicies;
	}

	private OrganizationRole createRole(OrganizationPolicies defaultPolicies, String name,
			LeafDefaultRoleConfig roleConfig) {
		OrganizationRole role = new OrganizationRole();
		role.setName(name);
		role.setCreatorDefault(roleConfig.isCreatorDefault());
		role.setOtherDefault(roleConfig.isOtherDefault());

		List<OrganizationPolicy> rights = defaultPolicies.copyPolicies();
		for (Map.Entry<String, String> policyDefaultValue : roleConfig.getPolicies().entrySet()) {
			for (OrganizationPolicy right : rights) {
				if (right.getName().equals(policyDefaultValue.getKey())) {
					right.setValue(policyDefaultValue.getValue());
				}
			}
		}
		role.setRights(rights);
		return role;
	}

	public String extractCreatorDefaultRole(OrganizationPolicies organizationPolicies) {
		for (OrganizationRole role : organizationPolicies.getRoles()) {
			if (role.isCreatorDefault()) {
				return role.getName();
			}
		}
		return null;
	}

	public String extractOtherDefaultRole(OrganizationPolicies organizationPolicies) {
		for (OrganizationRole role : organizationPolicies.getRoles()) {
			if (role.isOtherDefault()) {
				return role.getName();
			}
		}
		return null;
	}

	public LeafOrganization createNewRole(String name) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);
		OrganizationRole newRole = this.createDefaultRole(name);

		organization.getPolicies().getRoles().forEach(role -> {
			if (name.equals(role.getName())) {
				throw new BadRequestException("A role with this name already exists");
			}
		});

		organization.getPolicies().getRoles().add(newRole);
		return this.organizationRepository.save(organization);
	}

	private OrganizationRole createDefaultRole(String name) {
		OrganizationPolicies defaultPolicies = this.extractDefaultPolicies();

		Map<String, LeafDefaultRoleConfig> configRoles = organizationConfig.getDefaultRoles();
		for (LeafDefaultRoleConfig configRole : configRoles.values()) {
			if (configRole.isOtherDefault()) {
				OrganizationRole newRole = this.createRole(defaultPolicies, name, configRole);
				newRole.setCreatorDefault(false);
				newRole.setOtherDefault(false);
				return newRole;
			}
		}
		throw new InternalServerErrorException("Organization policies configuration does not contains a default role");
	}

	public LeafOrganization updateRole(String roleName, OrganizationRole roleUpdate) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);

		OrganizationRole updatedRole = organization.getPolicies().getRoles().stream()
				.filter(role -> roleName.equals(role.getName())).findFirst()
				.orElseThrow(() -> new NotFoundException("This role does not exists in this organization"));

		updatedRole.updateWith(roleUpdate);

		return this.organizationRepository.save(organization);
	}

	public LeafOrganization deleteRole(String roleName) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);

		OrganizationRole deletedRole = organization.getPolicies().getRoles().stream()
				.filter(role -> roleName.equals(role.getName())).findFirst()
				.orElseThrow(() -> new NotFoundException("This role does not exists in this organization"));

		// Remove role
		organization.getPolicies().setRoles(organization.getPolicies().getRoles().stream()
				.filter(role -> deletedRole != role).collect(Collectors.toList()));

		// Re-qualify member with deleted role with default role
		String defaultRoleName = this.extractOtherDefaultRole(organization.getPolicies());
		organization.getMembers().forEach(member -> {
			if (member.getRole().equals(deletedRole.getName())) {
				member.setRole(defaultRoleName);
			}
		});

		return this.organizationRepository.save(organization);
	}
}