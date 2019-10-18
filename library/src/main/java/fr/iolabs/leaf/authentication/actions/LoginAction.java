package fr.iolabs.leaf.authentication.actions;

import fr.iolabs.leaf.common.utils.StringHasher;

public class LoginAction {
    public String email;
    public String password;


    public void hashPassword() {
        this.password = StringHasher.hashString(this.password);
    }
}
