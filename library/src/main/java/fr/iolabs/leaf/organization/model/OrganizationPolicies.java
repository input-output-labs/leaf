package fr.iolabs.leaf.organization.model;

import java.util.List;

public class OrganizationPolicies {
	private List<OrganizationRole> roles;
	private List<OrganizationPolicy> policies;

	public List<OrganizationRole> getRoles() {
		return roles;
	}

	public void setRoles(List<OrganizationRole> roles) {
		this.roles = roles;
	}

	public List<OrganizationPolicy> getPolicies() {
		return policies;
	}

	public void setPolicies(List<OrganizationPolicy> policies) {
		this.policies = policies;
	}
}
