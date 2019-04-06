package fr.iolabs.leafdemo.account;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.annotations.UseAccount;

@UseAccount
public class Account extends LeafAccount {
    public Account() {
        super();
    }
}
