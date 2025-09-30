package fr.iolabs.leaf.organization.membership;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.organization.model.LeafOrganization;

public class LeafOrganizationCandidatureEnableEvent extends ApplicationEvent {
	private static final long serialVersionUID = 3339748867382226670L;

	private LeafOrganization organization;
	private boolean enabled;

	public LeafOrganizationCandidatureEnableEvent(Object source, LeafOrganization organization, boolean enabled) {
		super(source);
		this.organization = organization;
		this.enabled = enabled;
	}

	public LeafOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(LeafOrganization organization) {
		this.organization = organization;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
