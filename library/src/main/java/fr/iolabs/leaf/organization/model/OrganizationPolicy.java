package fr.iolabs.leaf.organization.model;

public class OrganizationPolicy {
	private String order;
	private String name;
	private String type;
	private String value;
	
	public OrganizationPolicy copy() {
		OrganizationPolicy copy = new OrganizationPolicy();
		copy.order = this.order;
		copy.name = this.name;
		copy.type = this.type;
		copy.value = this.value;
		return copy;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
