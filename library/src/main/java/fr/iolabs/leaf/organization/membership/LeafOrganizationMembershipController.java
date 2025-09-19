package fr.iolabs.leaf.organization.membership;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.annotations.LeafEligibilityCheck;
import fr.iolabs.leaf.common.annotations.MandatoryOrganization;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.organization.actions.SetMemberRoleAction;
import fr.iolabs.leaf.organization.membership.actions.AddUserToOrganizationAction;
import fr.iolabs.leaf.organization.membership.actions.InviteUserToOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.dto.OrganizationCandidatureData;
import fr.iolabs.leaf.organization.model.dto.OrganizationInvitationData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class LeafOrganizationMembershipController {

	@Autowired
	private LeafPrivacyService leafPrivacyService;
	
	@Autowired
	private LeafOrganizationMembershipService organizationMembershipService;

	@CrossOrigin
	@AdminOnly
	@PostMapping("/{organizationId}/members")
	public void addUsersToOrganization(@PathVariable String organizationId,
			@RequestBody AddUserToOrganizationAction action) {
		this.organizationMembershipService.addUsersToOrganization(organizationId, action.getAccountIds());
	}

	@CrossOrigin
	@GetMapping("/{organizationId}/members")
	public List<LeafAccount> listOrganizationUsers(@PathVariable String organizationId) {
		return this.leafPrivacyService.protectAccounts(this.organizationMembershipService.listUsers(organizationId));
	}

	@CrossOrigin
	@LeafEligibilityCheck({"manageMembers"})
	@DeleteMapping("/selected/members/{accountId}")
	public LeafOrganization removeUserFromOrganization(@PathVariable String accountId) {
		if (accountId == null || accountId.isBlank()) {
			throw new BadRequestException("Account id must not be empty");
		}
		return this.organizationMembershipService.removeUserFromOrganization(accountId);
	}

	@CrossOrigin
	@LeafEligibilityCheck({"manageMembers"})
	@PutMapping("/selected/members/{accountId}/role")
	public LeafOrganization setUserRole(@PathVariable String accountId, @RequestBody SetMemberRoleAction action) {
		if (accountId == null || accountId.isBlank()) {
			throw new BadRequestException("Account id must not be empty");
		}
		return this.organizationMembershipService.setUserRole(accountId, action.getRole());
	}

	@CrossOrigin
	@LeafEligibilityCheck({"manageMembers"})
	@PostMapping("/selected/invitations")
	public LeafOrganization inviteUserToOrganization(@RequestBody InviteUserToOrganizationAction action) {
		return this.organizationMembershipService.inviteUserToOrganization(action.getEmail());
	}

	@CrossOrigin
	@GetMapping("/{organizationId}/invitations/{email}")
	public OrganizationInvitationData getInvitationData(@PathVariable String organizationId,
			@PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		return this.organizationMembershipService.getInvitationData(organizationId, email);
	}

	@CrossOrigin
	@LeafEligibilityCheck({"manageMembers"})
	@PostMapping("/selected/invitations/{email}/cancel")
	public void cancelInvitation(@PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.cancelInvitation(email);
	}

	@CrossOrigin
	@PostMapping("/{organizationId}/invitations/{email}/accept")
	public void acceptInvitation(@PathVariable String organizationId, @PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.acceptInvitation(organizationId, email);
	}

	@CrossOrigin
	@PostMapping("/{organizationId}/invitations/{email}/decline")
	public void declineInvitation(@PathVariable String organizationId, @PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.declineInvitation(organizationId, email);
	}

	@CrossOrigin
	@MandatoryOrganization
	@PostMapping("/selected/candidature-management/enable")
	public LeafOrganization enableCandidatureManagement(@RequestBody boolean enable) {
		return this.organizationMembershipService.enableCandidatureManagement(enable);
	}

	@CrossOrigin
	@PostMapping("/{organizationId}/candidature-management/candidate")
	public void candidateToOrganization(@PathVariable String organizationId, @RequestBody String role) {
		this.organizationMembershipService.candidateToOrganization(organizationId, role);
	}

	@CrossOrigin
	@GetMapping("/{organizationId}/candidature-management/{role}")
	public OrganizationCandidatureData getOrganizationCandidatureData(@PathVariable String organizationId, @PathVariable String role) {
		return this.organizationMembershipService.getOrganizationCandidatureData(organizationId, role);
	}

	@CrossOrigin
	@MandatoryOrganization
	@PostMapping("/selected/candidature-management/candidatures/{email}/accept")
	public void acceptCandidature(@PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.acceptCandidature(email);
	}

	@CrossOrigin
	@PostMapping("/selected/candidature-management/candidatures/{email}/decline")
	public void declineCandidature(@PathVariable String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be empty");
		}
		this.organizationMembershipService.declineCandidature(email);
	}

}