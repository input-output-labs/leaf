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

import java.util.ArrayList;
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
	public List<LeafOrganization> listOrganizations(
			@RequestParam(required = false) String nameFilter,
			@RequestParam(required = false) Integer limit) {
		return this.organizationService.listAll(nameFilter, limit);
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/search")
	public OrganizationSearchResponse searchOrganizations(
			@RequestBody(required = false) OrganizationSearchCriteria criteria) {
		return this.organizationService.search(criteria != null ? criteria : new OrganizationSearchCriteria());
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping
	public LeafOrganization createOrganization(@RequestBody CreateOrganizationAction action) {
		return this.organizationService.create(action);
	}

	public LeafOrganization protectOrganization(LeafOrganization organization) {
		List<LeafOrganization> protectedOrganizations = this.protectOrganizations(List.of(organization));
		return protectedOrganizations.size() > 0 ? protectedOrganizations.get(0) : null;
	}

	public List<LeafOrganization> protectOrganizations(Iterable<LeafOrganization> organizations) {
		List<LeafOrganization> organizationList = StreamSupport.stream(organizations.spliterator(), false)
				.collect(Collectors.toList());
		List<LeafOrganization> organizationsReturned = new ArrayList<>();
		for (LeafOrganization organization : organizationList) {
			Map<String, LeafEligibility> eligibilities = new HashMap<>();
			this.organizationEligibilitiesService.getEligibilities(this.coreContext.getAccount(), organization,
					eligibilities, List.of("seeOrganization", "seeMembers", "seePolicies"));
			LeafEligibility seeOrganization = eligibilities.get("seeOrganization");
			LeafEligibility seeMembers = eligibilities.get("seeMembers");
			LeafEligibility seePolicies = eligibilities.get("seePolicies");
			boolean isAdmin = coreContext.getAccount() != null && coreContext.getAccount().isAdmin();
			boolean canSeeOrganization = isAdmin || seeOrganization != null && seeOrganization.eligible;
			boolean canSeeMembers = isAdmin || seeMembers != null && seeMembers.eligible;
			boolean canSeePolicies = isAdmin || seePolicies != null && seePolicies.eligible;
			
			if (!canSeeOrganization) {
				organization.setMetadata(null);
				organization.setModules(null);
			}
			if (canSeeOrganization) {
				organizationsReturned.add(organization);
			}
			if (!canSeeMembers) {
				organization.setMembers(null);
				organization.setInvitations(null);
			}
			if (!canSeePolicies) {
				organization.setPolicies(null);
			}
			
			if (!isAdmin) {
				organization.setGenericData(null);
			}
		}
		return organizationsReturned;
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
	@PostMapping("/{organizationId}/flags")
	public LeafOrganization updateFlags(@PathVariable String organizationId, @RequestBody List<String> flags) {
		Optional<LeafOrganization> optOrganization = this.organizationRepository.findById(organizationId);
		if (optOrganization.isEmpty()) {
			throw new NotFoundException("No existing organization with id=" + organizationId);
		}

		LeafOrganization organization = optOrganization.get();
		organization.setFlags(flags == null ? List.of() : flags);
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