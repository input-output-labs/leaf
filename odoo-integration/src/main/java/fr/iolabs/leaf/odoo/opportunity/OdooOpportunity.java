package fr.iolabs.leaf.odoo.opportunity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class OdooOpportunity {

	private Integer id;
	private String name;
	private String contactName;
	private String email;
	private String phone;
	private Double expectedRevenue;
	private String partnerName;
	private Integer stageId;
	private String stageName;
	private List<String> tags = new ArrayList<>();
	private ZonedDateTime createdAt;

	public OdooOpportunity() {}

	public OdooOpportunity(
		Integer id,
		String name,
		String contactName,
		String email,
		String phone,
		Double expectedRevenue,
		String partnerName
	) {
		this.id = id;
		this.name = name;
		this.contactName = contactName;
		this.email = email;
		this.phone = phone;
		this.expectedRevenue = expectedRevenue;
		this.partnerName = partnerName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Double getExpectedRevenue() {
		return expectedRevenue;
	}

	public void setExpectedRevenue(Double expectedRevenue) {
		this.expectedRevenue = expectedRevenue;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public Integer getStageId() {
		return stageId;
	}

	public void setStageId(Integer stageId) {
		this.stageId = stageId;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags != null ? tags : new ArrayList<>();
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
