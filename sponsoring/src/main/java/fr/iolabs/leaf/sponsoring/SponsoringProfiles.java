package fr.iolabs.leaf.sponsoring;

import java.util.Set;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;

public class SponsoringProfiles {
	private LeafAccountProfile sponsor;

	private Set<LeafAccountProfile> affiliates;
	
	public SponsoringProfiles() {
	}

	public LeafAccountProfile getSponsor() {
		return sponsor;
	}

	public void setSponsor(LeafAccountProfile sponsor) {
		this.sponsor = sponsor;
	}

	public Set<LeafAccountProfile> getAffiliates() {
		return affiliates;
	}

	public void setAffiliates(Set<LeafAccountProfile> affiliates) {
		this.affiliates = affiliates;
	}
}
