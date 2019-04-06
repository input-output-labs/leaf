package fr.iolabs.leaf.admin.whitelisting;

import org.springframework.data.annotation.Id;

public class AuthorizedEmail {

	@Id
	public String email;
	
	public AuthorizedEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
