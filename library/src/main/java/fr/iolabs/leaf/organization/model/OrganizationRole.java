package fr.iolabs.leaf.organization.model;

import java.util.List;

public class OrganizationRole {
	private boolean creatorDefault;
	private boolean otherDefault;
	private String name;
	private List<OrganizationPolicy> rights;

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

	public void updateWith(OrganizationRole roleUpdate) {
		if (roleUpdate.name != null && !roleUpdate.name.isBlank()) {
			this.name = roleUpdate.name;
		}
		if (roleUpdate.rights != null) {
			for (OrganizationPolicy right : this.rights) {
				for (OrganizationPolicy rightUpdate : roleUpdate.rights) {
					if (right.getName().equals(rightUpdate.getName())) {
						right.setValue(rightUpdate.getValue());
					}
				}
			}
		}
	}
}
