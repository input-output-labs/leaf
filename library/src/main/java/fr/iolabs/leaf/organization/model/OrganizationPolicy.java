package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.common.LeafPolicy;

public class OrganizationPolicy extends LeafPolicy {
	private String order;
	
	public OrganizationPolicy copy() {
		OrganizationPolicy copy = new OrganizationPolicy();
		copy.mergeFrom(this);
		copy.order = this.order;
		return copy;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
}
