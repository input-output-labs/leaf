package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/organizations")
public class LeafOrganizationReadController {
	@Autowired
	private LeafOrganizationService organizationService;

	@Autowired
	private LeafPrivacyService leafPrivacyService;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@CrossOrigin
	@AdminOnly
	@GetMapping
	public List<LeafOrganization> listOrganizations() {
		return this.organizationService.listAll();
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
	@GetMapping("/{organizationId}/users")
	public List<LeafAccount> listOrganizationUsers(@PathVariable String organizationId) {
		return this.leafPrivacyService.protectAccounts(this.organizationService.listUsers(organizationId));
	}
}