package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LeafOrganizationService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	public void create(CreateOrganizationAction action) {
		LeafOrganization organization = new LeafOrganization();
		organization.setName(action.getName());
		organization.setMetadata(new ResourceMetadata());

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));

		organizationRepository.save(organization);
	}
}