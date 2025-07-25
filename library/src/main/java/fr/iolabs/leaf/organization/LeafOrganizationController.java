package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.eligibilities.LeafEligibility;
import fr.iolabs.leaf.eligibilities.LeafOrganizationEligibilitiesComposer;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.policies.LeafOrganizationPoliciesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/organizations")
public class LeafOrganizationController {
	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafOrganizationService organizationService;

	@Autowired
	private LeafOrganizationPoliciesService organizationPoliciesService;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafOrganizationEligibilitiesComposer organizationEligibilitiesService;

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

	public LeafOrganization protectOrganization(LeafOrganization organization) {
		return this.protectOrganizations(List.of(organization)).get(0);
	}

	public List<LeafOrganization> protectOrganizations(Iterable<LeafOrganization> organizations) {
		List<LeafOrganization> organizationList = StreamSupport.stream(organizations.spliterator(), false)
				.collect(Collectors.toList());
		for (LeafOrganization organization : organizationList) {
			Map<String, LeafEligibility> eligibilities = new HashMap<>();
			this.organizationEligibilitiesService.getEligibilities(this.coreContext.getAccount(), organization,
					eligibilities, List.of("seeOrganization", "seeMembers", "seePolicies"));
			if (!eligibilities.get("seeOrganization").eligible) {
				organization.setMetadata(null);
				organization.setModules(null);
			}
			if (!eligibilities.get("seeMembers").eligible) {
				organization.setMembers(null);
				organization.setInvitations(null);
			}
			if (!eligibilities.get("seePolicies").eligible) {
				organization.setPolicies(null);
			}
			
			if (coreContext.getAccount() == null || !coreContext.getAccount().isAdmin()) {
				organization.setGenericData(null);
			}
		}
		return organizationList;
	}

	@CrossOrigin
	@GetMapping("/mine")
	public Iterable<LeafOrganization> listMyOrganizations() {
		return this
				.protectOrganizations(this.organizationService.getByIds(coreContext.getAccount().getOrganizationIds()));
	}

	@CrossOrigin
	@GetMapping("/{organizationId}")
	public LeafOrganization getOrganizationById(@PathVariable String organizationId) {
		return this.protectOrganization(this.organizationService.getById(organizationId).orElseThrow(NotFoundException::new));
	}

	@CrossOrigin
	@PostMapping("/{organizationId}/profile")
	public LeafOrganization updateProfile(@PathVariable String organizationId,
			@RequestBody LeafAccountProfile profile) {
		return this.protectOrganization(this.organizationService.updateProfile(organizationId, profile));
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/{organizationId}/genericData")
	public LeafOrganization updateGenericData(@PathVariable String organizationId, @RequestBody Map<String, String> genericData) {
		Optional<LeafOrganization> optOrganization = this.organizationRepository.findById(organizationId);
		if (optOrganization.isEmpty()) {
			throw new NotFoundException("No existing organization with id=" + organizationId);
		}

		LeafOrganization organization = optOrganization.get();
		if (organization.getGenericData() == null) {
			organization.setGenericData(new HashMap<>());
		}

		for(Map.Entry<String, String> entry : genericData.entrySet()) {
			organization.getGenericData().put(entry.getKey(), entry.getValue());
		}

		return this.protectOrganization(this.organizationRepository.save(organization));
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/all/policies/refresh")
	public void refreshAllOrganizationsPolicies() {
		List<LeafOrganization> all = this.organizationRepository.findAll();
		all.forEach((organization) -> this.organizationPoliciesService.refreshOrganizationPolicies(organization));
		this.organizationRepository.saveAll(all);
	}
}