package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class OrganizationMembership {
	private String accountId;
	private String role;
	private ResourceMetadata metadata;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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
