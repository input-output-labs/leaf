package fr.iolabs.leaf.organization.model.dto;

public class OrganizationCandidatureData {
	public static enum OrganizationCandidatureDataError {
		MISSING_ORGANIZATION,
		CANDIDATURE_DISABLED,
		INVALID_ROLE
	}
	private String organizationName;
	private OrganizationCandidatureDataError error;
	public String getOrganizationName() {
		return organizationName;
	}
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}
	public OrganizationCandidatureDataError getError() {
		return error;
	}
	public void setError(OrganizationCandidatureDataError error) {
		this.error = error;
	}
}
