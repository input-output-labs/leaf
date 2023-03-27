package fr.iolabs.leaf.common;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;

public class LeafAccountProfilePrivacyEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private String accountId;
	private LeafAccountProfile profile;

	public LeafAccountProfilePrivacyEvent(Object source, String accountId, LeafAccountProfile profile) {
		super(source);
		this.accountId = accountId;
		this.profile = profile;
	}

	public String accountId() {
		return accountId;
	}
	
	public LeafAccountProfile profile() {
		return profile;
	}
}