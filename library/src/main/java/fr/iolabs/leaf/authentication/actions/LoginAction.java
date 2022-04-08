package fr.iolabs.leaf.authentication.actions;

import fr.iolabs.leaf.common.utils.StringHasher;

public class LoginAction {
    private String email;
    private String password;

    public void hashPassword() {
        this.password = StringHasher.hashString(this.password);
    }

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
}
