package fr.iolabs.leaf.organization.model;

import java.util.List;

public class OrganizationRole {
	private boolean isDefault;
	private String name;
	private List<OrganizationPolicy> rights;

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<OrganizationPolicy> getRights() {
		return rights;
	}

	public void setRights(List<OrganizationPolicy> rights) {
		this.rights = rights;
	}
}
