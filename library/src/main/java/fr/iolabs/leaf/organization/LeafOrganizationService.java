package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LeafOrganizationService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

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

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));

		return organizationRepository.save(organization);
	}
}