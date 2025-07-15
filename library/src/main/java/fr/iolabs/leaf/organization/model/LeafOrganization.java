package fr.iolabs.leaf.organization.model;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.ILeafModular;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "organization")
public class LeafOrganization implements ILeafModular {
	@Id
	protected String id;

	protected String name;

	protected LeafAccountProfile profile;

	protected Map<String, Object> modules;

	protected Map<String, String> genericData;

	protected List<OrganizationMembership> members;

	protected List<OrganizationInvitation> invitations;
	
	protected OrganizationPolicies policies;

	protected ResourceMetadata metadata;

	public LeafOrganization() {
		this.modules = new HashMap<>();
		this.genericData = new HashMap<>();
		this.members = new ArrayList<>();
		this.invitations = new ArrayList<>();
	}

	public LeafOrganization(LeafOrganization from) {
		this();
		this.id = from.id;
		this.name = from.name;
		this.profile = from.profile;
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

	public LeafAccountProfile getProfile() {
		return profile;
	}

	public void setProfile(LeafAccountProfile profile) {
		this.profile = profile;
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

	public Map<String, String> getGenericData() {
		return genericData;
	}

	public void setGenericData(Map<String, String> genericData) {
		this.genericData = genericData;
	}

	public List<OrganizationInvitation> getInvitations() {
		return invitations;
	}

	public void setInvitations(List<OrganizationInvitation> invitations) {
		this.invitations = invitations;
	}

	public List<OrganizationMembership> getMembers() {
		return members;
	}

	public void setMembers(List<OrganizationMembership> members) {
		this.members = members;
	}

	public OrganizationPolicies getPolicies() {
		return policies;
	}

	public void setPolicies(OrganizationPolicies policies) {
		this.policies = policies;
	}
}
