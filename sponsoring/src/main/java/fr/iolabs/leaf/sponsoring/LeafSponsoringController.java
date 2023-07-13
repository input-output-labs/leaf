package fr.iolabs.leaf.sponsoring;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.authentication.privacy.LeafPrivacyService;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;

@RestController
@RequestMapping(path = "/api/account")
public class LeafSponsoringController {
	private static final int MAX_AFFILIATED_COUNT = 1000;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafModuleService moduleService;

	@Autowired
	private LeafSponsoringAccountRepository sponsoringAccountRepository;

	@Autowired
	private LeafPrivacyService privacyService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@CrossOrigin
	@PostMapping(path = "/me/sponsoring/sponsor")
	public LeafAccount setSponsor(@RequestBody SetSponsorAction setSponsorAction) {
		if (Strings.isBlank(setSponsorAction.getSponsorId())) {
			throw new BadRequestException("The sponsorId must not be empty");
		}

		Optional<LeafAccount> sponsorAccountOpt = this.sponsoringAccountRepository.findAccountBySponsorCode(setSponsorAction.getSponsorId());
		if (sponsorAccountOpt.isEmpty()) {
			throw new BadRequestException("The given sponsorId does not exists");
		}

		LeafAccount sponsorAccount = sponsorAccountOpt.get();
		LeafAccount myAccount = this.coreContext.getAccount();

		Sponsoring sponsorSponsoring = this.moduleService.get(Sponsoring.class, sponsorAccount);
		Sponsoring mySponsoring = this.moduleService.get(Sponsoring.class);
		this.applicationEventPublisher.publishEvent(new SponsoringRegistrationEvent(this, sponsorAccount.getId()));

		if (myAccount.getId().equals(sponsorAccount.getId())) {
			throw new BadRequestException("You cannot be your own sponsor");
		}

		if (mySponsoring.getSponsorId() != null) {
			throw new BadRequestException("This account already has a sponsor");
		}

		if (sponsorSponsoring.getAffiliatedIds().size() >= MAX_AFFILIATED_COUNT) {
			throw new BadRequestException("The sponsor has already reach it maximum count of affiliated");
		}

		mySponsoring.setSponsorId(sponsorAccount.getId());
		sponsorSponsoring.getAffiliatedIds().add(myAccount.getId());

		sponsoringAccountRepository.save(myAccount);
		sponsoringAccountRepository.save(sponsorAccount);

		this.applicationEventPublisher.publishEvent(new SponsoringRegistrationEvent(this, sponsorAccount.getId()));
		return myAccount;
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping(path = "/{accountId}/sponsoring/sponsorcode")
	public LeafAccount updateSponsorCode(@PathVariable String accountId, @RequestBody SetSponsorCodeAction setSponsorCodeAction) {
		Optional<LeafAccount> existingAccountWithSponsorCode = this.sponsoringAccountRepository.findAccountBySponsorCode(setSponsorCodeAction.getSponsorCode());
		if (existingAccountWithSponsorCode.isPresent()) {
			throw new UnauthorizedException("An account with this sponsor code already exists.");
		}
		Optional<LeafAccount> optAccount = this.sponsoringAccountRepository.findById(accountId);
		if (!optAccount.isPresent()) {
			throw new NotFoundException("No accounts with id: " + accountId);
		}
		LeafAccount account = optAccount.get();
		Sponsoring accountSponsoring = this.moduleService.get(Sponsoring.class, account);
		accountSponsoring.setSponsorCode(setSponsorCodeAction.getSponsorCode());
		
		return this.privacyService.protectAccount(sponsoringAccountRepository.save(account));
	}

	@CrossOrigin
	@GetMapping(path = "/me/sponsoring")
	public SponsoringProfiles getSponsoringProfiles() {
		Sponsoring mySponsoring = this.moduleService.get(Sponsoring.class);

		Set<String> accountIds = new HashSet<>();

		if (mySponsoring.getSponsorId() != null) {
			accountIds.add(mySponsoring.getSponsorId());
		}
		mySponsoring.getAffiliatedIds().forEach(id -> accountIds.add(id));

		Map<String, LeafAccountProfile> profiles = new HashMap<>();
		this.sponsoringAccountRepository.findAllById(accountIds).forEach((account) -> {
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
