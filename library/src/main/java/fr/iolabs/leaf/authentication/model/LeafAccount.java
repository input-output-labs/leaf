package fr.iolabs.leaf.authentication.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.iolabs.leaf.common.utils.StringHasher;

import java.util.Set;
import java.util.HashSet;

@Document(collection = "account")
public class LeafAccount {
    @Id
    protected String id;
    protected String email;

    protected String password;
    protected String username;
    protected String avatarUrl;
    protected String resetPasswordKey;
    protected Set<PrivateToken> privateTokens;
    protected Set<String> hashedSessionTokens;

	protected boolean admin;

    public LeafAccount() {
        this.admin = false;
        this.privateTokens = new HashSet<>();
        this.hashedSessionTokens = new HashSet<>();
    }

    public void hashPassword() {
        this.password = StringHasher.hashString(this.password);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
