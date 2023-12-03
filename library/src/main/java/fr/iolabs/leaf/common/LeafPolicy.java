package fr.iolabs.leaf.common;

public class LeafPolicy implements Cloneable {
	protected String name;
	protected String type;
	protected String value;
	
	public void mergeFrom(LeafPolicy that) {
		if (that.name != null ) {
			this.name = that.name;
		}
		if (that.type != null ) {
			this.type = that.type;
		}
		if (that.value != null ) {
			this.value = that.value;
		}
	}
	
	public LeafPolicy clone() {
		LeafPolicy clone = new LeafPolicy();
		clone.name = this.name;
		clone.type = this.type;
		clone.value = this.value;
		return clone;
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
