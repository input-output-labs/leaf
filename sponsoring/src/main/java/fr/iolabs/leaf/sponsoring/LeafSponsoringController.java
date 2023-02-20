package fr.iolabs.leaf.sponsoring;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;

@RestController
@RequestMapping(path = "/api/account/me/sponsoring")
public class LeafSponsoringController {
	private static final int MAX_AFFILIATED_COUNT = 1000;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafModuleService moduleService;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafPrivacyService privacyService;

	@CrossOrigin
	@PostMapping(path = "/sponsor")
	public LeafAccount setSponsor(@RequestBody SetSponsorAction setSponsorAction) {
		if (Strings.isBlank(setSponsorAction.getSponsorId())) {
			throw new BadRequestException("The sponsorId must not be empty");
		}

		Optional<LeafAccount> sponsorAccountOpt = this.accountRepository.findById(setSponsorAction.getSponsorId());
		if (sponsorAccountOpt.isEmpty()) {
			throw new BadRequestException("The given sponsorId does not exists");
		}

		LeafAccount sponsorAccount = sponsorAccountOpt.get();
		LeafAccount myAccount = this.coreContext.getAccount();

		Sponsoring sponsorSponsoring = this.moduleService.get(Sponsoring.class, sponsorAccount);
		Sponsoring mySponsoring = this.moduleService.get(Sponsoring.class);

		if (mySponsoring.getSponsorId() != null) {
			throw new BadRequestException("This account already has a sponsor");
		}

		if (sponsorSponsoring.getAffiliatedIds().size() >= MAX_AFFILIATED_COUNT) {
			throw new BadRequestException("The sponsor has already reach it maximum count of affiliated");
		}

		mySponsoring.setSponsorId(sponsorAccount.getId());
		sponsorSponsoring.getAffiliatedIds().add(myAccount.getId());

		accountRepository.save(myAccount);
		accountRepository.save(sponsorAccount);
		return myAccount;
	}

	@CrossOrigin
	@GetMapping
	public SponsoringProfiles getSponsoringProfiles() {
		Sponsoring mySponsoring = this.moduleService.get(Sponsoring.class);

		Set<String> accountIds = new HashSet<>();

		if (mySponsoring.getSponsorId() != null) {
			accountIds.add(mySponsoring.getSponsorId());
		}
		mySponsoring.getAffiliatedIds().forEach(id -> accountIds.add(id));

		Map<String, LeafAccountProfile> profiles = new HashMap<>();
		this.accountRepository.findAllById(accountIds).forEach((account) -> {
			this.privacyService.protectAccount(account);
			profiles.put(account.getId(), account.getProfile());
		});
		
		SponsoringProfiles sponsoringProfiles = new SponsoringProfiles();

		if (mySponsoring.getSponsorId() != null) {
			sponsoringProfiles.setSponsor(profiles.get(mySponsoring.getSponsorId()));
		}
		HashSet<LeafAccountProfile> affiliatedProfiles = new HashSet<>();
		mySponsoring.getAffiliatedIds().forEach(id -> affiliatedProfiles.add(profiles.get(id)));
		sponsoringProfiles.setAffiliates(affiliatedProfiles);
		
		return sponsoringProfiles;
	}
}
