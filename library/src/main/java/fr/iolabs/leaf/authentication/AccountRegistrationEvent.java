package fr.iolabs.leaf.authentication;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class AccountRegistrationEvent extends ApplicationEvent {
	private LeafAccount account;

	public AccountRegistrationEvent(Object source, LeafAccount account) {
		super(source);
		this.account = account;
	}

	public LeafAccount account() {
		return account;
	}
}
