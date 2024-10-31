package fr.iolabs.leaf.authentication.model.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LeafAccountProfile {
	private String username;
	private String avatarUrl;
	private String firstname;
	private String lastname;
	private String phoneNumber;
	private LeafAddress address;
	private Boolean corporate;
	private String companyName;
	private String taxId;
    private String registrationNumber;
	
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
		this.registrationNumber = from.registrationNumber;
		this.companyName = from.companyName;
	}
	
	@JsonIgnore
	public String getDisplayName() {
		boolean firstnameDefined = this.firstname != null && !this.firstname.isBlank();
		boolean lastnameDefined = this.lastname != null && !this.lastname.isBlank();
		if (firstnameDefined || lastnameDefined) {
			StringJoiner sj = new StringJoiner(" ");
			if (firstnameDefined) {
				sj.add(this.firstname);
			}
			if (lastnameDefined) {
				sj.add(this.lastname);
			}
			return sj.toString();
		} else if (this.username != null && !this.username.isBlank()) {
			return this.username;
		}
		return null;
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

	public boolean isCorporate() {
		return this.corporate != null && this.corporate;
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
	
	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("username", this.username);
		object.put("avatarUrl", this.avatarUrl);
		object.put("firstname", this.firstname);
		object.put("lastname", this.lastname);
		object.put("phoneNumber", this.phoneNumber);
		object.put("companyName", this.companyName);
		object.put("address", this.address != null ? this.address.toMap() : null);
		object.put("corporate", this.corporate != null ? this.corporate.booleanValue() : null);
		object.put("taxId", this.taxId);
		object.put("registrationNumber", this.registrationNumber);
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
		if (updates.registrationNumber != null) {
			this.registrationNumber = updates.registrationNumber;
		}
		if (updates.companyName != null) {
			this.companyName = updates.companyName;
		}
	}
	
	public boolean isValid() {
		boolean valid = true;
		if (this.phoneNumber != null && !this.phoneNumber.matches("^\\+[1-9]\\d{1,14}$")) {
			valid = false;
		}
		return valid;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
