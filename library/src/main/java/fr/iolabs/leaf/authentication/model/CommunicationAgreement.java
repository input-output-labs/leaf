package fr.iolabs.leaf.authentication.model;

import java.util.HashSet;
import java.util.Set;

public class CommunicationAgreement {
	private Set<String> unsubscription = new HashSet<>();

	public Set<String> getUnsubscription() {
		return unsubscription;
	}

	public void setUnsubscription(Set<String> unsubscription) {
		this.unsubscription = unsubscription;
	}

	public void unsubscribe(String type) {
		this.unsubscription.add(type.toLowerCase());
	}
}
