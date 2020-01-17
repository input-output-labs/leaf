package fr.iolabs.leaf;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class LeafContext {
    private LeafAccount account;

    public LeafAccount getAccount() {
        return account;
    }

    public void setAccount(LeafAccount account) {
        this.account = account;
    }
}