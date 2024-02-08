package fr.iolabs.leaf.authentication.model.profile;

import java.util.HashMap;
import java.util.Map;

public class LeafAccountProfile {
	private String username;
	private String avatarUrl;
	private String firstname;
	private String lastname;
	private String phoneNumber;
	private LeafAddress address;
	private Boolean corporate;
	private String taxId;
	
	public LeafAccountProfile() {}
	
	public LeafAccountProfile(LeafAccountProfile from) {
		this.username = from.username;
		this.avatarUrl = from.avatarUrl;
		this.firstname = from.firstname;
		this.lastname = from.lastname;
		this.phoneNumber = from.phoneNumber;
		this.address = from.address;
		this.corporate = from.corporate;
		this.taxId = from.taxId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public LeafAddress getAddress() {
		return address;
	}
	public void setAddress(LeafAddress address) {
		this.address = address;
	}

	public Boolean getCorporate() {
		return corporate;
	}

	public void setCorporate(Boolean corporate) {
		this.corporate = corporate;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("username", this.username);
		object.put("avatarUrl", this.avatarUrl);
		object.put("firstname", this.firstname);
		object.put("lastname", this.lastname);
		object.put("phoneNumber", this.phoneNumber);
		object.put("address", this.address != null ? this.address.toMap() : null);
		return object;
	}

	public void updateWith(LeafAccountProfile updates) {
		if (updates.username != null) {
			this.username = updates.username;
		}
		if (updates.avatarUrl != null) {
			this.avatarUrl = updates.avatarUrl;
		}
		if (updates.firstname != null) {
			this.firstname = updates.firstname;
		}
		if (updates.lastname != null) {
			this.lastname = updates.lastname;
		}
		if (updates.phoneNumber != null) {
			this.phoneNumber = updates.phoneNumber;
		}
		if (updates.address != null) {
			if (this.address != null) {
				this.address.updateWith(updates.address);
			} else {
				this.address = LeafAddress.from(updates.address);
			}
		}
		if (updates.corporate != null) {
			this.corporate = updates.corporate;
		}
		if (updates.taxId != null) {
			this.taxId = updates.taxId;
		}
	}
}
