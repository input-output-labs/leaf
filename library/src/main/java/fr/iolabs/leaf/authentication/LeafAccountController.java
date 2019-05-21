package fr.iolabs.leaf.authentication;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.JWT;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.PasswordChanger;
import fr.iolabs.leaf.authentication.model.PasswordResetter;

@RestController
@RequestMapping("/api/account")
public class LeafAccountController {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private LeafAccountService accountService;

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST)
    public JWT regiterUser(@RequestBody LeafAccount account) {
        System.out.println("REGISTER");
        return new JWT(this.accountService.register(account));
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/login")
    public JWT login(@RequestBody LeafAccount account) {
        return new JWT(this.accountService.login(account));
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/me")
    public LeafAccount getUser() {
        LeafAccount me = this.coreContext.getAccount();
        me.setPassword("******");
        return me;
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/password")
    public LeafAccount changePassword(@RequestBody PasswordChanger passwordChanger) {
        return this.accountService.changePassword(passwordChanger);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/username")
    public LeafAccount changeName(@RequestBody String newName) {
        return this.accountService.changeName(newName);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/avatar")
    public LeafAccount changeAvatarUrl(@RequestBody String newAvatarUrl) {
        return this.accountService.changeAvatarUrl(newAvatarUrl);
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/sendresetpasswordkey")
    public ResponseEntity<Void> sendResetPasswordKey(@RequestBody String email) {
        this.accountService.sendResetPasswordKey(email);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin
    @PermitAll
    @RequestMapping(method = RequestMethod.POST, path = "/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetter passwordResetter) {
        this.accountService.resetPassword(passwordResetter);
        return ResponseEntity.noContent().build();
    }
}