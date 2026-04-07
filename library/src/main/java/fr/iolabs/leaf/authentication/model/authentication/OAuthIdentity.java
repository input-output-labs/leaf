package fr.iolabs.leaf.authentication.model.authentication;

import java.util.Objects;

public class OAuthIdentity {

	private String provider;
	private String providerUserId;
	private String email;

	public OAuthIdentity() {
	}

	public OAuthIdentity(String provider, String providerUserId, String email) {
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.email = email;
	}

	public OAuthIdentity(OAuthIdentity from) {
		this.provider = from.provider;
		this.providerUserId = from.providerUserId;
		this.email = from.email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OAuthIdentity that = (OAuthIdentity) o;
		return Objects.equals(provider, that.provider) && Objects.equals(providerUserId, that.providerUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(provider, providerUserId);
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
