package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.context.ApplicationEvent;

public class OrganizationProfileUpdateEvent extends ApplicationEvent {
	private final LeafOrganization organization;

	public OrganizationProfileUpdateEvent(Object source, LeafOrganization organization) {
		super(source);
		this.organization = organization;
	}

	public LeafOrganization getOrganization() {
		return organization;
	}
}
