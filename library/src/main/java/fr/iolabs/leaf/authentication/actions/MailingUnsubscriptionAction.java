package fr.iolabs.leaf.authentication.actions;

import fr.iolabs.leaf.common.emailing.CommunicationType;

public class MailingUnsubscriptionAction {
	private String email;
	private CommunicationType type;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public CommunicationType getType() {
		return type;
	}

	public void setType(CommunicationType type) {
		this.type = type;
	}
}
