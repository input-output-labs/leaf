package fr.iolabs.leaf.authentication.actions;

public class RegistrationAction {
	private String email;
	private String password;
	private String username;
	private String avatarUrl;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email != null ? email.toLowerCase() : null;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
}
