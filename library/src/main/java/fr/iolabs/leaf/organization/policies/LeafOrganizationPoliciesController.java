package fr.iolabs.leaf.organization.policies;

import fr.iolabs.leaf.common.annotations.LeafEligibilityCheck;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.organization.membership.actions.AddRoleToOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/selected/policies")
public class LeafOrganizationPoliciesController {

	@Autowired
	private LeafOrganizationPoliciesService organizationPoliciesService;

	@CrossOrigin
	@LeafEligibilityCheck({"managePolicies"})
	@PostMapping("/roles")
	public LeafOrganization createNewRole(@RequestBody AddRoleToOrganizationAction action) {
		String newRoleName = action.getName();
		if (newRoleName == null || newRoleName.isBlank()) {
			throw new BadRequestException("Name must not be empty");
		}
		return this.organizationPoliciesService.createNewRole(newRoleName);
	}

	@CrossOrigin
	@LeafEligibilityCheck({"managePolicies"})
	@PutMapping("/roles/{roleName}")
	public LeafOrganization updateRole(@PathVariable String roleName, @RequestBody OrganizationRole role) {
		if (roleName == null || roleName.isBlank()) {
			throw new BadRequestException("Role name must not be empty");
		}
		return this.organizationPoliciesService.updateRole(roleName, role);
	}

	@CrossOrigin
	@LeafEligibilityCheck({"managePolicies"})
	@DeleteMapping("/roles/{roleName}")
	public LeafOrganization deleteRole(@PathVariable String roleName) {
		if (roleName == null || roleName.isBlank()) {
			throw new BadRequestException("Role name must not be empty");
		}
		return this.organizationPoliciesService.deleteRole(roleName);
	}
}