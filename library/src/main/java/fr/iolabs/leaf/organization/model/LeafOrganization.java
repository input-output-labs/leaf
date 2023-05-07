package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "organization")
public class LeafOrganization {
	@Id
	protected String id;

	protected String name;

	protected Map<String, Object> modules;

	protected ResourceMetadata metadata;

	public LeafOrganization() {
		this.modules = new HashMap<>();
	}

	public LeafOrganization(LeafOrganization from) {
		this();
		this.id = from.id;
		this.name = from.name;
		this.metadata = from.metadata;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourceMetadata getMetadata() {
		if (this.metadata == null) {
			this.metadata = ResourceMetadata.create();
		}
		return this.metadata;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}

	public Map<String, Object> getModules() {
		return modules;
	}

	public void setModules(Map<String, Object> modules) {
		this.modules = modules;
	}
}
