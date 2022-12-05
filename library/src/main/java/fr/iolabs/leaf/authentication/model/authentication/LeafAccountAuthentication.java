package fr.iolabs.leaf.authentication.model.authentication;

import java.util.HashSet;
import java.util.Set;

import fr.iolabs.leaf.common.utils.StringHasher;

public class LeafAccountAuthentication {
	protected String password;
	protected String resetPasswordKey;
	protected Set<PrivateToken> privateTokens;
	protected Set<String> hashedSessionTokens;
	
	public LeafAccountAuthentication() {
		this.privateTokens = new HashSet<>();
		this.hashedSessionTokens = new HashSet<>();
	}
	
	public LeafAccountAuthentication(LeafAccountAuthentication from) {
		this.password = from.password;
		this.resetPasswordKey = from.resetPasswordKey;
		this.privateTokens = from.privateTokens;
		this.hashedSessionTokens = from.hashedSessionTokens;
	}

	public void hashPassword() {
		this.password = StringHasher.hashString(this.password);
	}

	public void generateResetPasswordKey() {
		String key = System.currentTimeMillis() + this.privateTokens.size() + "" + this.hashedSessionTokens.size();
		String shortHashKey = StringHasher.hashString(key).substring(0, 8);
		this.resetPasswordKey = shortHashKey;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
}
