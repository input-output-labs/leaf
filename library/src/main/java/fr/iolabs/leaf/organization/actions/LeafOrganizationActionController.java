package fr.iolabs.leaf.organization.actions;

import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.organization.LeafOrganizationService;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization")
public class LeafOrganizationActionController {
	@Autowired
	private LeafOrganizationService organizationService;

	@CrossOrigin
	@AdminOnly
	@PostMapping
	public LeafOrganization createOrganization(@RequestBody CreateOrganizationAction action) {
		return this.organizationService.create(action);
	}
}