package fr.iolabs.leaf.authentication.model;

import org.springframework.data.mongodb.core.mapping.Document;

import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.utils.StringHasher;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Document(collection = "account")
public class LeafAccount extends LeafUser {
	protected String email;
	protected LeafAccountAuthentication authentication;
	@Deprecated
	protected String password;
	@Deprecated
	protected String resetPasswordKey;
	@Deprecated
	protected Set<PrivateToken> privateTokens;
	@Deprecated
	protected Set<String> hashedSessionTokens;

	protected LeafAccountProfile profile;
	protected Set<String> organizationIds;
	protected CommunicationAgreement communication = new CommunicationAgreement();
	protected AccountVerification accountVerification;
	protected Map<String, Object> modules;
	protected ResourceMetadata metadata;

	protected boolean admin;

	public LeafAccount() {
		this.admin = false;
		this.authentication = new LeafAccountAuthentication();
		this.privateTokens = new HashSet<>();
		this.hashedSessionTokens = new HashSet<>();
		this.profile = new LeafAccountProfile();
		this.modules = new HashMap<>();
		this.organizationIds = new HashSet<>();
		this.accountVerification = new AccountVerification();
	}

	public LeafAccount(LeafAccount from) {
		super(from);
		this.email = from.email;
		this.admin = from.admin;
		this.authentication = from.authentication;
		this.privateTokens = from.privateTokens;
		this.hashedSessionTokens = from.hashedSessionTokens;
		this.profile = from.profile;
		this.communication = from.communication;
		this.modules = from.modules;
		this.metadata = from.metadata;
		this.organizationIds = from.organizationIds;
		this.accountVerification = from.accountVerification;
	}

	public LeafAccountAuthentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(LeafAccountAuthentication authentication) {
		this.authentication = authentication;
	}

	public LeafAccountProfile getProfile() {
		return profile;
	}

	public void setProfile(LeafAccountProfile profile) {
		this.profile = profile;
	}

	@Deprecated
	public void hashPassword() {
		this.setPassword(StringHasher.hashString(this.password));
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email != null ? email.toLowerCase() : null;
	}

	@Deprecated
	public String getPassword() {
		return password;
	}

	@Deprecated
	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Deprecated
	public void generateResetPasswordKey() {
		String key = System.currentTimeMillis() + this.email;
		String shortHashKey = StringHasher.hashString(key).substring(0, 8);
		this.resetPasswordKey = shortHashKey;
	}

	@Deprecated
	public String getResetPasswordKey() {
		return resetPasswordKey;
	}

	@Deprecated
	public void setResetPasswordKey(String resetPasswordKey) {
		this.resetPasswordKey = resetPasswordKey;
	}

	@Deprecated
	public Set<PrivateToken> getPrivateTokens() {
		return privateTokens;
	}

	@Deprecated
	public void setPrivateTokens(Set<PrivateToken> privateTokens) {
		this.privateTokens = privateTokens;
	}

	@Deprecated
	public Set<String> getHashedSessionTokens() {
		return hashedSessionTokens;
	}

	@Deprecated
	public void setHashedSessionTokens(Set<String> hashedSessionTokens) {
		this.hashedSessionTokens = hashedSessionTokens;
	}

	public Map<String, Object> getModules() {
		return modules;
	}

	public void setModules(Map<String, Object> modules) {
		this.modules = modules;
	}

	public CommunicationAgreement getCommunication() {
		return communication;
	}

	public void setCommunication(CommunicationAgreement communication) {
		this.communication = communication;
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

	public Set<String> getOrganizationIds() {
		return organizationIds;
	}

	public void setOrganizationIds(Set<String> organizationIds) {
		this.organizationIds = new HashSet<>(organizationIds);
	}
	
	public AccountVerification getAccountVerification() {
		return accountVerification;
	}

	public void setAccountVerification(AccountVerification accountVerification) {
		this.accountVerification = accountVerification;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("email", this.email);
		object.put("profile", this.profile.toMap());
		return object;
	}
}
