package fr.iolabs.leaf.admin;

import java.util.List;
import java.util.stream.Collectors;

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

import fr.iolabs.leaf.admin.whitelisting.AuthorizedEmail;
import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.annotations.AdminOnly;

@RestController
@RequestMapping("/api/admin")
public class LeafAdminController<T extends LeafAccount> {

	@Autowired
	private LeafAdminService<T> adminService;

	@Autowired
	private WhitelistingService whitelistingService;

	@AdminOnly
	@CrossOrigin
	@GetMapping("/authorizedemails")
	public List<AuthorizedEmail> listAllAuthorizedEmails() {
		return this.whitelistingService.listAllAuthorizedEmails();
	}

	@AdminOnly
	@CrossOrigin
	@PostMapping("/authorizedemails")
	public ResponseEntity<Void> addAuthorizedEmails(@RequestBody List<String> emails) {
		List<AuthorizedEmail> authorizedEmails = getEmails(emails);

		this.whitelistingService.addAuthorizedEmails(authorizedEmails);
		return ResponseEntity.noContent().build();
	}

	@AdminOnly
	@CrossOrigin
	@PostMapping("/authorizedemails/remove")
	public ResponseEntity<Void> removeAuthorizedEmails(@RequestBody List<String> emails) {
		List<AuthorizedEmail> authorizedEmails = getEmails(emails);

		this.whitelistingService.removeAuthorizedEmails(authorizedEmails);
		return ResponseEntity.noContent().build();
	}

	private List<AuthorizedEmail> getEmails(List<String> emails) {
		return emails.stream().map(AuthorizedEmail::new).collect(Collectors.toList());
	}

	@AdminOnly
	@CrossOrigin
	@PostMapping("/admin")
	public ResponseEntity<Void> addAdmin(@RequestBody String email) {
		this.adminService.addAdmin(email);
		return ResponseEntity.noContent().build();
	}

	@AdminOnly
	@CrossOrigin
	@DeleteMapping("/admin/{email:.*}")
	public ResponseEntity<Void> removeAdmin(@PathVariable String email) {
		this.adminService.removeAdmin(email);
		return ResponseEntity.noContent().build();
	}
}
