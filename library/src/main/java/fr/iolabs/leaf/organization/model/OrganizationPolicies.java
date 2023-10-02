package fr.iolabs.leaf.organization.model;

import java.util.ArrayList;
import java.util.List;

public class OrganizationPolicies {
	private List<OrganizationRole> roles;
	private List<OrganizationPolicy> policies;
	
	public OrganizationPolicies() {
		this.roles = new ArrayList<>();
		this.policies = new ArrayList<>();
	}

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

	public List<OrganizationPolicy> copyPolicies() {
		List<OrganizationPolicy> copiedPolicies = new ArrayList<>();
		for (OrganizationPolicy policy : this.policies) {
			copiedPolicies.add(policy.copy());
		}
		return copiedPolicies;
	}
}
