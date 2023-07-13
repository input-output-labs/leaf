package fr.iolabs.leaf.sponsoring;

import java.util.HashSet;
import java.util.Set;

public class Sponsoring {
	private String sponsorCode;
	private String sponsorId;

	private Set<String> affiliatedIds;

	public Sponsoring() {
		this.sponsorId = null;
		this.affiliatedIds = new HashSet<>();
	}

	public String getSponsorCode() {
		return sponsorCode;
	}

	public void setSponsorCode(String sponsorCode) {
		this.sponsorCode = sponsorCode;
	}

	public String getSponsorId() {
		return sponsorId;
	}

	public void setSponsorId(String sponsorId) {
		this.sponsorId = sponsorId;
	}

	public Set<String> getAffiliatedIds() {
		return affiliatedIds;
	}

	public void setAffiliatedIds(Set<String> affiliatedIds) {
		this.affiliatedIds = affiliatedIds;
	}
}
