package fr.iolabs.leaf.authentication.actions;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;

@RestController
@RequestMapping("/api/account")
public class LeafAccountActionController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafAccountService accountService;

	@Autowired
	private LeafPrivacyService privacyHelper;

	@CrossOrigin
	@PermitAll
	@PostMapping
	public JWT registerUser(@RequestBody RegistrationAction userRegistration) {
		return new JWT(this.accountService.registerAndLogin(userRegistration));
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/user")
	public LeafAccount registerUserWithoutLogin(@RequestBody RegistrationAction userRegistration) {
		return this.accountService.register(userRegistration);
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/login")
	public JWT login(@RequestBody LoginAction accountLogin) {
		return new JWT(this.accountService.login(accountLogin));
	}

	@CrossOrigin
	@AdminOnly
	@DeleteMapping("/{accountId}")
	public void deleteUser(@PathVariable String accountId) {
		this.accountService.deleteUser(accountId);
	}

	@CrossOrigin
	@PostMapping("/me/password")
	public LeafAccount changePassword(@RequestBody ChangePasswordAction passwordChanger) {
		return this.privacyHelper.protectAccount(this.accountService.changePassword(passwordChanger));
	}

	@CrossOrigin
	@PostMapping("/me/privatetokens")
	public JWT addPrivateToken(@RequestBody PrivateToken privateToken) {
		return this.accountService.addPrivateToken(privateToken);
	}

	@CrossOrigin
	@DeleteMapping("/me/privatetokens/{name}")
	public LeafAccount revokePrivateToken(@PathVariable String name) {
		return this.privacyHelper.protectAccount(this.accountService.revokePrivateToken(name));
	}

	@CrossOrigin
	@PostMapping("/me/username")
	public LeafAccount changeName(@RequestBody String newName) {
		return this.privacyHelper.protectAccount(this.accountService.changeName(newName));
	}

	@CrossOrigin
	@PostMapping("/me/profile")
	public LeafAccount updateProfile(@RequestBody LeafAccountProfile profile) {
		return this.privacyHelper.protectAccount(this.accountService.updateProfile(profile));
	}

	@CrossOrigin
	@PostMapping("/me/avatar")
	public LeafAccount changeAvatarUrl(@RequestBody String newAvatarUrl) {
		return this.privacyHelper.protectAccount(this.accountService.changeAvatarUrl(newAvatarUrl));
	}

	@CrossOrigin
	@PostMapping("/me/verification")
	public LeafAccount accountVerification(@RequestBody AccountVerification accountVerification) {
		return this.privacyHelper.protectAccount(this.accountService.accountVerification(accountVerification));
	}

	@CrossOrigin
	@PostMapping("/me/verification/email/send")
	public LeafAccount sendEmailVerificationCode() {
		return this.privacyHelper.protectAccount(this.accountService.sendEmailVerificationCode());
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/sendresetpasswordkey")
	public ResponseEntity<Void> sendResetPasswordKey(@RequestBody String email) {
		if(email != null) {
			this.accountService.sendResetPasswordKey(email.toLowerCase(), false);
		} else {
			throw new UnauthorizedException();
		}
		return ResponseEntity.noContent().build();
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/resetPassword")
	public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordAction resetPasswordAction) {
		this.accountService.resetPassword(resetPasswordAction);
		return ResponseEntity.noContent().build();
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/mailings/unsubscription")
	public ResponseEntity<Void> unsubscribeFromEmail(@RequestBody MailingUnsubscriptionAction resetPasswordAction) {
		this.accountService.unsubscribeFromEmail(resetPasswordAction);
		return ResponseEntity.noContent().build();
	}
}