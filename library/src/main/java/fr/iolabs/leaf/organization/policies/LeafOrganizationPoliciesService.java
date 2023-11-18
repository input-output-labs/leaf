package fr.iolabs.leaf.organization.policies;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	
	public void refreshOrganizationPolicies(LeafOrganization organization) {
		System.out.println("--------------------------------------------------------");
		System.out.println("Organization: " + organization.getName());
		OrganizationPolicies defaultPolicies = this.extractDefaultPolicies();
		
		System.out.println("");
		System.out.println("### Policies update");

		for(Entry<String, OrganizationPolicy> entry: this.organizationConfig.getPolicies().entrySet()) {
			System.out.println("## Policy: " + entry.getKey());
			boolean shouldAdd = true;
			for (OrganizationPolicy existingPolicy : organization.getPolicies().getPolicies()) {
				if (existingPolicy.getName().equals(entry.getKey())) {
					shouldAdd = false;
					System.out.println("Not new ! Ignoring...");
					break;
				}
			}
			if (shouldAdd) {
				System.out.println("New ! Adding...");
				OrganizationPolicy policy = entry.getValue();
				policy.setName(entry.getKey());
				
				organization.getPolicies().getPolicies().add(policy);
				System.out.println("Policy added");

				System.out.println("## Roles updates: " + entry.getKey());
				for (OrganizationRole role: organization.getPolicies().getRoles()) {
					System.out.println("# Role: " + role.getName());
					OrganizationPolicy policyCopy = policy.copy();
					
					
					LeafDefaultRoleConfig defaultRole = this.organizationConfig.getDefaultRoles().get(role.getName());
					if (defaultRole == null) {
						String creatorDefaultName = this.organizationConfig.getCreatorDefaultName();
						if (role.getName().equals(creatorDefaultName)) {
							defaultRole = this.organizationConfig.getDefaultRoles().get(creatorDefaultName);
						} else {
							String otherDefaultName = this.organizationConfig.getOtherDefaultName();
							defaultRole = this.organizationConfig.getDefaultRoles().get(otherDefaultName);
						}
					}
					if (defaultRole != null) {
						String value = defaultRole.getPolicies().get(policyCopy.getName());
						System.out.println("Adding policy default value for role: " + value);
						policyCopy.setValue(value);
					}
					boolean shouldAddRight = true;
					for (OrganizationPolicy right: role.getRights()) {
						if (right.getName().equals(policyCopy.getName())) {
							shouldAddRight = false;
							break;
						}
					}
					if (shouldAddRight) {
						role.getRights().add(policyCopy);
					}
				}
			}
		}
		System.out.println("");
		System.out.println("### Roles update");
		for (Entry<String, LeafDefaultRoleConfig> entry: this.organizationConfig.getDefaultRoles().entrySet()) {
			boolean shouldAdd = true;
			for (OrganizationRole existingRole: organization.getPolicies().getRoles()) {
				if (existingRole.getName().equals(entry.getKey())) {
					shouldAdd = false;
					System.out.println("Not new ! Ignoring...");
					break;
				}
			}
			if (shouldAdd) {
				System.out.println("New ! Adding...");
				OrganizationRole newRole = this.createRole(defaultPolicies, entry.getKey(), entry.getValue());
				organization.getPolicies().getRoles().add(newRole);
			}
		}
	}
}