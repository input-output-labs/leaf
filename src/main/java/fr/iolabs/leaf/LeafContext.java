package fr.iolabs.leaf;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class LeafContext {
    private LeafAccount account;

    @SuppressWarnings("unchecked")
    public <T extends LeafAccount> T getAccount() {
        return (T) account;
    }

    public void setAccount(LeafAccount account) {
        this.account = account;
    }
}