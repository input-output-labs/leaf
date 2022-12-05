package fr.iolabs.leaf.authentication.model.profile;

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
}
