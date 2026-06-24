package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.context.ApplicationEvent;

public class OrganizationCreatedEvent extends OrganizationCreationEvent {
	public OrganizationCreatedEvent(Object source, LeafOrganization organization) {
		super(source, organization);
	}
}
