package fr.iolabs.leaf.authentication;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import fr.iolabs.leaf.authentication.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping
    public JWT regiterUser(@RequestBody LeafAccount account) {
        return new JWT(this.accountService.registerAndLogin(account));
    }

    @CrossOrigin
    @PermitAll
    @PostMapping("/login")
    public JWT login(@RequestBody LeafAccount account) {
        return new JWT(this.accountService.login(account));
    }

    @CrossOrigin
    @GetMapping("/me")
    public LeafAccount getUser() {
        LeafAccount me = this.coreContext.getAccount();
        return me.obstrufy();
    }

    @CrossOrigin
    @PostMapping("/me/password")
    public LeafAccount changePassword(@RequestBody PasswordChanger passwordChanger) {
        return this.accountService.changePassword(passwordChanger).obstrufy();
    }

    @CrossOrigin
    @PostMapping("/me/privatetokens")
    public JWT addPrivateToken(@RequestBody PrivateToken privateToken) {
        return this.accountService.addPrivateToken(privateToken);
    }

    @CrossOrigin
    @DeleteMapping("/me/privatetokens/{name}")
    public LeafAccount revokePrivateToken(@PathVariable String name) {
        return this.accountService.revokePrivateToken(name).obstrufy();
    }

    @CrossOrigin
    @PostMapping("/me/username")
    public LeafAccount changeName(@RequestBody String newName) {
        return this.accountService.changeName(newName).obstrufy();
    }

    @CrossOrigin
    @PostMapping("/me/avatar")
    public LeafAccount changeAvatarUrl(@RequestBody String newAvatarUrl) {
        return this.accountService.changeAvatarUrl(newAvatarUrl).obstrufy();
    }

    @CrossOrigin
    @PermitAll
    @PostMapping("/sendresetpasswordkey")
    public ResponseEntity<Void> sendResetPasswordKey(@RequestBody String email) {
        this.accountService.sendResetPasswordKey(email);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin
    @PermitAll
    @PostMapping("/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetter passwordResetter) {
        this.accountService.resetPassword(passwordResetter);
        return ResponseEntity.noContent().build();
    }
}