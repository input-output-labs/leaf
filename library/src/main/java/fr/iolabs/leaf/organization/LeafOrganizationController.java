package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.membership.LeafOrganizationMembershipService;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/organizations")
public class LeafOrganizationController {
	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafPrivacyService leafPrivacyService;

	@Autowired
	private LeafOrganizationService organizationService;

	@Autowired
	private LeafOrganizationMembershipService organizationMembershipService;

	@CrossOrigin
	@AdminOnly
	@GetMapping
	public List<LeafOrganization> listOrganizations() {
		return this.organizationService.listAll();
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping
	public LeafOrganization createOrganization(@RequestBody CreateOrganizationAction action) {
		return this.organizationService.create(action);
	}

	@CrossOrigin
	@GetMapping("/mine")
	public Iterable<LeafOrganization> listMyOrganizations() {
		return this.organizationService.getByIds(coreContext.getAccount().getOrganizationIds());
	}

	@CrossOrigin
	@GetMapping("/{organizationId}")
	public LeafOrganization getOrganizationById(@PathVariable String organizationId) {
		return this.organizationService.getById(organizationId).orElseThrow(NotFoundException::new);
	}

	@CrossOrigin
	@GetMapping("/{organizationId}/members")
	public List<LeafAccount> listOrganizationUsers(@PathVariable String organizationId) {
		return this.leafPrivacyService.protectAccounts(this.organizationMembershipService.listUsers(organizationId));
	}
}