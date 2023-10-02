package fr.iolabs.leaf.organization.model.config;

import java.util.Map;

public class LeafDefaultRoleConfig {
	private boolean creatorDefault;
	private boolean otherDefault;
	private Map<String, String> policies;

	public boolean isCreatorDefault() {
		return creatorDefault;
	}

	public void setCreatorDefault(boolean creatorDefault) {
		this.creatorDefault = creatorDefault;
	}

	public boolean isOtherDefault() {
		return otherDefault;
	}

	public void setOtherDefault(boolean otherDefault) {
		this.otherDefault = otherDefault;
	}

	public Map<String, String> getPolicies() {
		return policies;
	}

	public void setPolicies(Map<String, String> policies) {
		this.policies = policies;
	}
}
