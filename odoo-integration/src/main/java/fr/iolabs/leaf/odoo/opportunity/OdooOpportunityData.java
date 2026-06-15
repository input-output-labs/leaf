package fr.iolabs.leaf.odoo.opportunity;

import fr.iolabs.leaf.odoo.OdooIntegrationException;
import org.springframework.util.StringUtils;

public class OdooOpportunityData {

	private String name;
	private String contactName;
	private String email;
	private String phone;
	private String description;
	private Double expectedRevenue;
	private String partnerName;
	private String street;
	private String city;
	private String zip;
	private int priorityStars;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public int getPriorityStars() {
		return priorityStars;
	}

	public void setPriorityStars(int priorityStars) {
		this.priorityStars = priorityStars;
	}

	public void validate() {
		if (!StringUtils.hasText(name)) {
			throw new OdooIntegrationException("Opportunity name is required");
		}
	}
}
