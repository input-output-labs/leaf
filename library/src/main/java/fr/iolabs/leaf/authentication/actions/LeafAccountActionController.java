package fr.iolabs.leaf.authentication.actions;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.authentication.read.LeafAccountDTO;
import fr.iolabs.leaf.authentication.read.PrivateTokenDTO;
import fr.iolabs.leaf.common.annotations.AdminOnly;

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

	@CrossOrigin
	@PermitAll
	@PostMapping
	public JWT registerUser(@RequestBody RegistrationAction userRegistration) {
		return new JWT(this.accountService.registerAndLogin(userRegistration));
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
	public LeafAccountDTO changePassword(@RequestBody ChangePasswordAction passwordChanger) {
		return LeafAccountDTO.from(this.accountService.changePassword(passwordChanger));
	}

	@CrossOrigin
	@PostMapping("/me/privatetokens")
	public JWT addPrivateToken(@RequestBody PrivateTokenDTO privateToken) {
		return this.accountService.addPrivateToken(privateToken.toPrivateToken());
	}

	@CrossOrigin
	@DeleteMapping("/me/privatetokens/{name}")
	public LeafAccountDTO revokePrivateToken(@PathVariable String name) {
		return LeafAccountDTO.from(this.accountService.revokePrivateToken(name));
	}

	@CrossOrigin
	@PostMapping("/me/username")
	public LeafAccountDTO changeName(@RequestBody String newName) {
		return LeafAccountDTO.from(this.accountService.changeName(newName));
	}

	@CrossOrigin
	@PostMapping("/me/avatar")
	public LeafAccountDTO changeAvatarUrl(@RequestBody String newAvatarUrl) {
		return LeafAccountDTO.from(this.accountService.changeAvatarUrl(newAvatarUrl));
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
	public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordAction resetPasswordAction) {
		this.accountService.resetPassword(resetPasswordAction);
		return ResponseEntity.noContent().build();
	}
}