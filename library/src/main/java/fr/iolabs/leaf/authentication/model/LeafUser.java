package fr.iolabs.leaf.authentication.model;

import org.springframework.data.annotation.Id;

public class LeafUser {
	@Id
	protected String id;
	@Deprecated
	protected String username;
	@Deprecated
	protected String avatarUrl;

	public LeafUser() {}
	public LeafUser(LeafUser from) {
		this.id = from.id;
		this.username = from.username;
		this.avatarUrl = from.avatarUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Deprecated
	public String getUsername() {
		return username;
	}

	@Deprecated
	public void setUsername(String username) {
		this.username = username;
	}

	@Deprecated
	public String getAvatarUrl() {
		return avatarUrl;
	}

	@Deprecated
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
