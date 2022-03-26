package fr.iolabs.leaf.authentication;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class AccountDeletionEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1919229812523038699L;

	private LeafAccount account;

	public AccountDeletionEvent(Object source, LeafAccount account) {
		super(source);
		this.account = account;
	}

	public LeafAccount account() {
		return account;
	}
}
