package fr.iolabs.leaf.authentication.model;

import org.springframework.data.mongodb.core.mapping.Document;

import fr.iolabs.leaf.common.utils.StringHasher;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Document(collection = "account")
public class LeafAccount extends LeafUser {
	protected String email;
	protected String password;
	protected String resetPasswordKey;
	protected Set<PrivateToken> privateTokens;
	protected Set<String> hashedSessionTokens;
	protected Map<String, Object> modules;
	protected ResourceMetadata metadata;

	protected boolean admin;

	public LeafAccount() {
		this.admin = false;
		this.privateTokens = new HashSet<>();
		this.hashedSessionTokens = new HashSet<>();
		this.modules = new HashMap<>();
	}

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

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void generateResetPasswordKey() {
		String key = System.currentTimeMillis() + this.email;
		String shortHashKey = StringHasher.hashString(key).substring(0, 8);
		this.resetPasswordKey = shortHashKey;
	}

	public String getResetPasswordKey() {
		return resetPasswordKey;
	}

	public void setResetPasswordKey(String resetPasswordKey) {
		this.resetPasswordKey = resetPasswordKey;
	}

	public Set<PrivateToken> getPrivateTokens() {
		return privateTokens;
	}

	public void setPrivateTokens(Set<PrivateToken> privateTokens) {
		this.privateTokens = privateTokens;
	}

	public Set<String> getHashedSessionTokens() {
		return hashedSessionTokens;
	}

	public void setHashedSessionTokens(Set<String> hashedSessionTokens) {
		this.hashedSessionTokens = hashedSessionTokens;
	}

	public Map<String, Object> getModules() {
		return modules;
	}

	public void setModules(Map<String, Object> modules) {
		this.modules = modules;
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

	public void merge(LeafAccount account) {
		if (account.email != null) {
			this.email = account.email;
		}
		if (account.username != null) {
			this.username = account.username;
		}
		if (account.password != null) {
			this.password = account.password;
		}
		if (account.avatarUrl != null) {
			this.avatarUrl = account.avatarUrl;
		}
		this.admin = account.admin;
	}
}
