package fr.iolabs.leaf.sponsoring;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SponsoringRegistrationEventListener implements ApplicationListener<SponsoringRegistrationEvent> {

	@Override
	public void onApplicationEvent(SponsoringRegistrationEvent event) {
		String sponsorId = event.getSponsorId();
//		System.out.println("INTO SPONSOR EVENT: " + sponsorId);
	}
}
