package fr.iolabs.leaf.authentication;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import fr.iolabs.leaf.authentication.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.iolabs.leaf.LeafContext;

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
        return new JWT(this.accountService.registerAndLogin(account));
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
        return me.obstrufy();
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/password")
    public LeafAccount changePassword(@RequestBody PasswordChanger passwordChanger) {
        return this.accountService.changePassword(passwordChanger).obstrufy();
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/privatetokens")
    public JWT addPrivateToken(@RequestBody PrivateToken privateToken) {
        return this.accountService.addPrivateToken(privateToken);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.DELETE, path = "/me/privatetokens/{name}")
    public LeafAccount revokePrivateToken(@PathVariable String name) {
        return this.accountService.revokePrivateToken(name).obstrufy();
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/username")
    public LeafAccount changeName(@RequestBody String newName) {
        return this.accountService.changeName(newName).obstrufy();
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/me/avatar")
    public LeafAccount changeAvatarUrl(@RequestBody String newAvatarUrl) {
        return this.accountService.changeAvatarUrl(newAvatarUrl).obstrufy();
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