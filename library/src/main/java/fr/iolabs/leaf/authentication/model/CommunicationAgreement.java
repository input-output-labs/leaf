package fr.iolabs.leaf.authentication.model;

import fr.iolabs.leaf.common.emailing.CommunicationType;

public class CommunicationAgreement {
	private boolean commercialAllowed;
	private boolean notificationsAllowed;

	public boolean isCommercialAllowed() {
		return commercialAllowed;
	}

	public void setCommercialAllowed(boolean commercialAllowed) {
		this.commercialAllowed = commercialAllowed;
	}

	public boolean isNotificationsAllowed() {
		return notificationsAllowed;
	}

	public void setNotificationsAllowed(boolean notificationsAllowed) {
		this.notificationsAllowed = notificationsAllowed;
	}

	public void unsubscribe(CommunicationType type) {
		switch (type) {
		case COMMERCIAL:
			this.commercialAllowed = false;
			break;
		case NOTIFICATIONS:
			this.notificationsAllowed = false;
			break;
		default:
			break;
		}
	}
}
