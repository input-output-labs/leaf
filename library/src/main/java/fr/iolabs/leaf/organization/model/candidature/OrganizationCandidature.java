package fr.iolabs.leaf.organization.model.candidature;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class OrganizationCandidature {
	private String accountId;
	private String email;
	private OrganizationCandidatureStatus status;
	private String role;
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

	public OrganizationCandidatureStatus getStatus() {
		return status;
	}

	public void setStatus(OrganizationCandidatureStatus status) {
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
