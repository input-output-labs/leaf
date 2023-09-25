package fr.iolabs.leaf;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.organization.model.LeafOrganization;

public class LeafContext {
    private LeafAccount account;
    private LeafOrganization organization;

    public LeafAccount getAccount() {
        return account;
    }

    public void setAccount(LeafAccount account) {
        this.account = account;
    }

	public LeafOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(LeafOrganization organization) {
		this.organization = organization;
	}
}