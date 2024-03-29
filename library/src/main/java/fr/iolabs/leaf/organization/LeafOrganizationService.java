package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.organization.policies.LeafOrganizationPoliciesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;

@Service
public class LeafOrganizationService {
	@Resource(name = "coreContext")
	private LeafContext coreContext;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationPoliciesService policiesService;

	public List<LeafOrganization> listAll() {
		return organizationRepository.findAll();
	}

	public Optional<LeafOrganization> getById(String id) {
		return organizationRepository.findById(id);
	}

	public Iterable<LeafOrganization> getByIds(Set<String> organizationIds) {
		if (organizationIds != null && organizationIds.size() > 0) {
			return this.organizationRepository.findAllById(organizationIds);
		}
		return List.of();
	}

	public LeafOrganization create(CreateOrganizationAction action) {
		LeafOrganization organization = new LeafOrganization();

		organization.setName(action.getName());
		organization.setMetadata(ResourceMetadata.create());
		organization.setPolicies(this.policiesService.createDefaultPolicies());

		OrganizationMembership firstMember = new OrganizationMembership();
		firstMember.setAccountId(coreContext.getAccount().getId());
		firstMember.setRole(this.policiesService.extractCreatorDefaultRole(organization.getPolicies()));
		firstMember.getMetadata();

		organization.getMembers().add(firstMember);

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));

		LeafOrganization savedOrganization = organizationRepository.save(organization);

		coreContext.getAccount().getOrganizationIds().add(savedOrganization.getId());
		this.accountRepository.save(coreContext.getAccount());

		return savedOrganization;
	}

	public LeafOrganization updateProfile(String organizationId, LeafAccountProfile profile) {
		LeafOrganization organization = this.getById(organizationId).orElseThrow(NotFoundException::new);
		if (organization.getProfile() == null) {
			organization.setProfile(profile);
		} else {
			organization.getProfile().updateWith(profile);
		}
		if (profile != null && profile.getDisplayName() != null) {
			organization.setName(profile.getDisplayName());
		}

		this.applicationEventPublisher.publishEvent(new OrganizationProfileUpdateEvent(this, organization));

		return this.organizationRepository.save(organization);
	}
}