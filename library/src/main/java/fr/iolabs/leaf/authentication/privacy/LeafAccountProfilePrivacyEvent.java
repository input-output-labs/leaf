package fr.iolabs.leaf.authentication.privacy;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;

public class LeafAccountProfilePrivacyEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private LeafAccountProfile profile;

	public LeafAccountProfilePrivacyEvent(Object source, LeafAccountProfile profile) {
		super(source);
		this.profile = profile;
	}

	public LeafAccountProfile profile() {
		return profile;
	}
}