package fr.iolabs.leaf.sponsoring;

import org.springframework.context.ApplicationEvent;

public class SponsoringRegistrationEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private String sponsorId;

	public SponsoringRegistrationEvent(Object source, String sponsorId) {
		super(source);
	}

	public String getSponsorId() {
		return this.sponsorId;
	}
}
