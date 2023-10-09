package fr.iolabs.leaf.organization.membership;

import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.organization.actions.SetMemberRoleAction;
import fr.iolabs.leaf.organization.membership.actions.AddUserToOrganizationAction;
import fr.iolabs.leaf.organization.membership.actions.InviteUserToOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.dto.OrganizationInvitationData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{organizationId}")
public class LeafOrganizationMembershipController {
	@Autowired
	private LeafOrganizationMembershipService organizationMembershipService;

	@CrossOrigin
	@AdminOnly
	@PostMapping("/members")
	public void addUsersToOrganization(@PathVariable String organizationId,
			@RequestBody AddUserToOrganizationAction action) {
		this.organizationMembershipService.addUsersToOrganization(organizationId, action.getAccountIds());
	}

	@CrossOrigin
	@AdminOnly
	@DeleteMapping("/members/{accountId}")
	public LeafOrganization removeUserFromOrganization(@PathVariable String organizationId, @PathVariable String accountId) {
		if (accountId == null || accountId.isBlank()) {
			throw new BadRequestException("Account id must not be empty");
		}
		return this.organizationMembershipService.removeUserFromOrganization(organizationId, accountId);
	}

	@CrossOrigin
	@AdminOnly
	@PutMapping("/members/{accountId}/role")
	public LeafOrganization setUserRole(@PathVariable String organizationId, @PathVariable String accountId, @RequestBody SetMemberRoleAction action) {
		if (accountId == null || accountId.isBlank()) {
			throw new BadRequestException("Account id must not be empty");
		}
		return this.organizationMembershipService.setUserRole(organizationId, accountId, action.getRole());
	}

	@CrossOrigin
	@PostMapping("/invitations")
	public LeafOrganization inviteUserToOrganization(@PathVariable String organizationId,
			@RequestBody InviteUserToOrganizationAction action) {
		return this.organizationMembershipService.inviteUserToOrganization(organizationId, action.getEmail());
	}

	@CrossOrigin
	@GetMapping("/invitations/{email}")
	public OrganizationInvitationData getInvitationData(@PathVariable String organizationId,
			@PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		return this.organizationMembershipService.getInvitationData(organizationId, email);
	}

	@CrossOrigin
	@PostMapping("/invitations/{email}/cancel")
	public void cancelInvitation(@PathVariable String organizationId, @PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.cancelInvitation(organizationId, email);
	}

	@CrossOrigin
	@PostMapping("/invitations/{email}/accept")
	public void acceptInvitation(@PathVariable String organizationId, @PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.acceptInvitation(organizationId, email);
	}

	@CrossOrigin
	@PostMapping("/invitations/{email}/decline")
	public void declineInvitation(@PathVariable String organizationId, @PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.declineInvitation(organizationId, email);
	}
}