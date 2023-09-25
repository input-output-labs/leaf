package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class OrganizationInvitation {
	private String accountId;
	private String email;
	private OrganizationInvitationStatus status;
	private ResourceMetadata metadata;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public OrganizationInvitationStatus getStatus() {
		return status;
	}

	public void setStatus(OrganizationInvitationStatus status) {
		this.status = status;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return this.metadata;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}
}
