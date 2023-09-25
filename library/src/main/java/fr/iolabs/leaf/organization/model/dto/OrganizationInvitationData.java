package fr.iolabs.leaf.organization.model.dto;

import fr.iolabs.leaf.organization.model.OrganizationInvitation;

public class OrganizationInvitationData {
	private String name;
	private OrganizationInvitation invitation;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OrganizationInvitation getInvitation() {
		return invitation;
	}

	public void setInvitation(OrganizationInvitation invitation) {
		this.invitation = invitation;
	}
}
