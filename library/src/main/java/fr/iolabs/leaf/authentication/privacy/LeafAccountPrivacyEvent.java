package fr.iolabs.leaf.authentication.privacy;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class LeafAccountPrivacyEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private LeafAccount account;

	public LeafAccountPrivacyEvent(Object source, LeafAccount account) {
		super(source);
		this.account = account;
	}

	public LeafAccount account() {
		return this.account;
	}
}