package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.common.LeafPolicy;

public class OrganizationPolicy extends LeafPolicy {
	private String order;
	private String category;
	
	public OrganizationPolicy copy() {
		OrganizationPolicy copy = new OrganizationPolicy();
		copy.mergeFrom(this);
		copy.order = this.order;
		copy.category = this.category;
		return copy;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
