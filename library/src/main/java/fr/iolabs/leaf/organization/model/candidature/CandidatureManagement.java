package fr.iolabs.leaf.organization.model.candidature;

import java.util.ArrayList;
import java.util.List;

public class CandidatureManagement {
	private boolean enabled;
	private List<OrganizationCandidature> candidatures;

	public CandidatureManagement() {
		this.candidatures = new ArrayList<OrganizationCandidature>();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public List<OrganizationCandidature> getCandidatures() {
		return candidatures;
	}
	public void setCandidatures(List<OrganizationCandidature> candidatures) {
		this.candidatures = candidatures;
	}
}
