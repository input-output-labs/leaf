package fr.iolabs.leaf.setup;

import java.util.List;
import java.util.Map;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.eligibilities.LeafEligibility;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.organization.model.LeafOrganization;

public class SetupResponse {
	private LeafAccount user;
	private List<LeafNotification> notifications;
	private Iterable<LeafOrganization> organizations;
	private Map<String, LeafEligibility> eligibilities;

	public LeafAccount getUser() {
		return user;
	}

	public void setUser(LeafAccount user) {
		this.user = user;
	}

	public List<LeafNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<LeafNotification> notifications) {
		this.notifications = notifications;
	}

	public Iterable<LeafOrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Iterable<LeafOrganization> organizations) {
		this.organizations = organizations;
	}

	public Map<String, LeafEligibility> getEligibilities() {
		return eligibilities;
	}

	public void setEligibilities(Map<String, LeafEligibility> eligibilities) {
		this.eligibilities = eligibilities;
	}
}
