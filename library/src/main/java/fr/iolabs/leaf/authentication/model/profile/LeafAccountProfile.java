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
	
	public LeafAccountProfile() {}
	
	public LeafAccountProfile(LeafAccountProfile from) {
		this.username = from.username;
		this.avatarUrl = from.avatarUrl;
		this.firstname = from.firstname;
		this.lastname = from.lastname;
		this.phoneNumber = from.phoneNumber;
		this.address = from.address;
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
}
