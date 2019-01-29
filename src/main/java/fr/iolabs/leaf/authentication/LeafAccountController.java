package fr.iolabs.leaf.authentication;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.JWT;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.PasswordChanger;
import fr.iolabs.leaf.authentication.model.PasswordResetter;

public class LeafAccountController<T extends LeafAccount> {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private LeafAccountService<T> accountService;

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/account")
    public JWT regiterUser(@RequestBody T account) {
        return new JWT(this.accountService.register(account));
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/account/login")
    public JWT login(@RequestBody T account) {
        return new JWT(this.accountService.login(account));
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/account/me")
    public T getUser() {
        T me = this.coreContext.getAccount();
        me.setPassword("******");
        return me;
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/account/me/password")
    public T changePassword(@RequestBody PasswordChanger passwordChanger) {
        return this.accountService.changePassword(passwordChanger);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/account/me/username")
    public T changeName(@RequestBody String newName) {
        return this.accountService.changeName(newName);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/account/me/avatar")
    public T changeAvatarUrl(@RequestBody String newAvatarUrl) {
        return this.accountService.changeAvatarUrl(newAvatarUrl);
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/account/sendresetpasswordkey")
    public ResponseEntity<Void> sendResetPasswordKey(@RequestBody String email) {
        this.accountService.sendResetPasswordKey(email);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/account/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetter passwordResetter) {
        this.accountService.resetPassword(passwordResetter);
        return ResponseEntity.noContent().build();
    }
}