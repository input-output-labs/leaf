package fr.iolabs.leaf.organization.actions;

import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.LeafOrganizationService;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

	@CrossOrigin
	@GetMapping
	public List<LeafOrganization> listOrganizations() {
		return this.organizationService.listAll();
	}

	@CrossOrigin
	@GetMapping("/{organizationId}")
	public LeafOrganization getOrganizationById(@PathVariable String organizationId) {
		return this.organizationService.getById(organizationId).orElseThrow(NotFoundException::new);
	}
}