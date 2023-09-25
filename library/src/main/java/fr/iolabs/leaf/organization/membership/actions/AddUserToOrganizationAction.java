package fr.iolabs.leaf.organization.membership.actions;

import java.util.Set;

public class AddUserToOrganizationAction {
	private Set<String> accountIds;

	public Set<String> getAccountIds() {
		return accountIds;
	}

	public void setAccountIds(Set<String> accountIds) {
		this.accountIds = accountIds;
	}
}
