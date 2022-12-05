package fr.iolabs.leaf.authentication.read;

import java.util.List;

import javax.annotation.Resource;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.annotations.AdminOnly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;

@RestController
@RequestMapping("/api/account")
public class LeafAccountReadController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafPrivacyService privacyHelper;

	@CrossOrigin
	@GetMapping("/all")
	@AdminOnly
	public List<LeafAccount> listUsers() {
		List<LeafAccount> accounts = this.accountRepository.findAll();
		return this.privacyHelper.protectAccounts(accounts);
	}

	@CrossOrigin
	@GetMapping("/me")
	public LeafAccount getUser() {
		LeafAccount me = this.coreContext.getAccount();
		return this.privacyHelper.protectAccount(me);
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping("/autocomplete")
	public List<LeafAccount> autocomplete(@RequestParam("input") String input) {
		return this.privacyHelper
				.protectAccounts(this.accountRepository.findByUsernameLike(input, PageRequest.of(0, 10)));
	}
}