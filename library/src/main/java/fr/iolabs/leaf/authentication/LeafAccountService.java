package fr.iolabs.leaf.authentication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.authentication.actions.LoginAction;
import fr.iolabs.leaf.authentication.actions.MailingUnsubscriptionAction;
import fr.iolabs.leaf.authentication.actions.RegistrationAction;
import fr.iolabs.leaf.authentication.actions.ResetPasswordAction;
import fr.iolabs.leaf.authentication.actions.AccountVerification;
import fr.iolabs.leaf.authentication.actions.ChangePasswordAction;
import fr.iolabs.leaf.common.utils.StringHasher;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;
import fr.iolabs.leaf.common.TokenService;
import fr.iolabs.leaf.common.emailing.LeafAccountEmailing;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Service
public class LeafAccountService {

	private static Logger logger = LoggerFactory.getLogger(LeafAccountService.class);

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Value("${leaf.appDomain}")
	private String appDomain;

	@Autowired
	private WhitelistingService whitelistingService;

	@Autowired
	private LeafAccountRepository accountRepository;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private LeafAccountEmailing accountEmailing;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private LeafNotificationService notificationService;

	private static final Integer GENERATED_PASSWORD_LENGTH = 8;

	private static final String DEFAULT_DOMAIN_NAME = "io-labs.fr";

	public LeafAccount me() {
		return this.coreContext.getAccount();
	}

	public String registerAndLogin(RegistrationAction userRegistration) {
		LeafAccount registeredAccount = this.register(userRegistration);
		logger.info("Account created with ID " + registeredAccount.getId());

		String sessionToken = this.createSessionAndCookie(registeredAccount);

		// Re-save account to register newly created session
		accountRepository.save(registeredAccount);

		return sessionToken;
	}

	public LeafAccount register(RegistrationAction userRegistration) {
		return userRegistration.isTemporary() ? this.registerTemporaryAccount(userRegistration) : this.register(userRegistration, false);
	}

	public LeafAccount register(RegistrationAction userRegistration, boolean isSystemAction) {
		if (Strings.isBlank(userRegistration.getEmail()) || Strings.isBlank(userRegistration.getPassword())) {
			throw new BadRequestException();
		}

		LeafAccount existingAccount = this.accountRepository
				.findAccountByEmail(userRegistration.getEmail().toLowerCase());
		if (existingAccount != null) {
			throw new BadRequestException();
		}

		if (!isSystemAction && this.whitelistingService.enabled()
				&& this.whitelistingService.isEmailAllowed(userRegistration.getEmail().toLowerCase())) {
			throw new UnauthorizedException();
		}

		LeafAccount instanciatedAccount = new LeafAccount();
		instanciatedAccount.setMetadata(ResourceMetadata.create());
		this.mergeRegistrationActionInLeafAccount(instanciatedAccount, userRegistration);

		this.applicationEventPublisher.publishEvent(new AccountRegistrationEvent(this, instanciatedAccount));

		instanciatedAccount.getAuthentication().hashPassword();

		LeafAccount createdAccount = accountRepository.save(instanciatedAccount);

		if (!isSystemAction) {
			this.notificationService.emit(
					LeafNotification.of("LEAF_ACCOUNT_REGISTRATION", createdAccount.getId(), createdAccount.toMap()));
		}

		return createdAccount;
	}

	public LeafAccount registerTemporaryAccount(RegistrationAction userRegistration) {
		LeafAccount instantiatedAccount = new LeafAccount();
		instantiatedAccount.setMetadata(ResourceMetadata.create());
		instantiatedAccount.setIsTemporary(true);
		this.mergeRegistrationActionInLeafAccount(instantiatedAccount, userRegistration);
		LeafAccount accountSaved = accountRepository.save(instantiatedAccount);
		String domainName = TemporaryAccountHelper.extractDomainName(this.appDomain);
		if(domainName == null) {
			domainName = DEFAULT_DOMAIN_NAME;
		}
		String tempEmail = accountSaved.getId() + "@" + domainName;
		accountSaved.setEmail(tempEmail);
		LeafAccountAuthentication authentication = new LeafAccountAuthentication();
		String pwd = TemporaryAccountHelper.generateComplexPassword(GENERATED_PASSWORD_LENGTH);
		authentication.setPassword(pwd);
		authentication.hashPassword();
		accountSaved.setAuthentication(authentication);
		return this.accountRepository.save(accountSaved);
	}


	public void deleteUser(String deletedAccountId) {
		Optional<LeafAccount> deletedAccountOpt = this.accountRepository.findById(deletedAccountId);

		if (deletedAccountOpt.isEmpty()) {
			throw new NotFoundException();
		}

		this.applicationEventPublisher.publishEvent(new AccountDeletionEvent(this, deletedAccountOpt.get()));

		this.accountRepository.delete(deletedAccountOpt.get());
	}

	private void mergeRegistrationActionInLeafAccount(LeafAccount account, RegistrationAction action) {
		account.setEmail(action.getEmail());
		account.getAuthentication().setPassword(action.getPassword());

		if (action.getUsername() != null) {
			account.getProfile().setUsername(action.getUsername());
		}
		if (action.getAvatarUrl() != null) {
			account.getProfile().setAvatarUrl(action.getAvatarUrl());
		}
		if (Strings.isBlank(account.getProfile().getUsername())) {
			account.getProfile().setUsername(action.getEmail());
		}
		account.setIsTemporary(action.isTemporary());
	}

	public String login(LoginAction accountLogin) {
		accountLogin.hashPassword();

		LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(accountLogin.getEmail());
		if (fetchedAccount == null
				|| !fetchedAccount.getAuthentication().getPassword().equals(accountLogin.getPassword())) {
			throw new UnauthorizedException();
		}

		String sessionToken = this.createSessionAndCookie(fetchedAccount);
		this.accountRepository.save(fetchedAccount);
		return sessionToken;
	}

	public String createSessionAndCookie(LeafAccount account) {
		String token = this.createSession(account);

		Cookie cookie = new Cookie("Authorization", token);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		this.response.addCookie(cookie);
		return token;
	}

	public String createSession(LeafAccount account) {
		String token = tokenService.createSessionJWT(account.getId());
		account.getAuthentication().getHashedSessionTokens().add(StringHasher.hashString(token));
		return token;
	}

	public String createSessionAndSaveAccount(LeafAccount account) {
		String token = this.createSession(account);
		this.accountRepository.save(account);
		return token;
	}

	public LeafAccount changePassword(ChangePasswordAction passwordChanger) {
		if (Strings.isBlank(passwordChanger.getOldPassword()) || Strings.isBlank(passwordChanger.getNewPassword())) {
			throw new BadRequestException();
		}

		String hashedOldPassword = StringHasher.hashString(passwordChanger.getOldPassword());

		LeafAccount me = this.coreContext.getAccount();
		if (!me.getAuthentication().getPassword().equals(hashedOldPassword)) {
			throw new UnauthorizedException();
		}

		me.getMetadata().updateLastModification();
		me.getAuthentication().setPassword(passwordChanger.getNewPassword());
		me.getAuthentication().hashPassword();

		return this.accountRepository.save(me);
	}

	/**
	 * Send an email to the user with a reset password key
	 * @param email
	 * @param wasTemporaryAccount: if the account was temporary, the email will be different. As the provided account has been already saved and set as non-temporary, we need to know if the email should be different
	 */
	public void sendResetPasswordKey(String email, boolean wasTemporaryAccount) {
		LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(email);
		if (fetchedAccount == null) {
			throw new UnauthorizedException();
		}

		fetchedAccount.getAuthentication().generateResetPasswordKey();

		this.accountRepository.save(fetchedAccount);

		this.accountEmailing.sendResetPasswordKey(fetchedAccount,
				fetchedAccount.getAuthentication().getResetPasswordKey(), wasTemporaryAccount);
	}

	public void resetPassword(ResetPasswordAction resetPasswordAction) {
		LeafAccount fetchedAccount = this.accountRepository.findAccountByResetPasswordKey(resetPasswordAction.getKey());

		if (fetchedAccount == null) {
			throw new UnauthorizedException();
		}

		fetchedAccount.getAuthentication().setPassword(resetPasswordAction.getPassword());
		fetchedAccount.getAuthentication().setResetPasswordKey(null);
		fetchedAccount.getAuthentication().hashPassword();

		this.accountRepository.save(fetchedAccount);
	}

	public LeafAccount changeName(String newName) {
		if (Strings.isBlank(newName)) {
			throw new BadRequestException();
		}

		LeafAccount me = this.coreContext.getAccount();
		me.getMetadata().updateLastModification();
		me.getProfile().setUsername(newName);

		return this.accountRepository.save(me);
	}

	public LeafAccount changeAvatarUrl(String newAvatarUrl) {
		if (Strings.isBlank(newAvatarUrl)) {
			throw new BadRequestException();
		}

		LeafAccount me = this.coreContext.getAccount();
		me.getMetadata().updateLastModification();
		me.getProfile().setAvatarUrl(newAvatarUrl);

		return this.accountRepository.save(me);
	}

	public JWT addPrivateToken(PrivateToken privateToken) {
		LeafAccount me = this.coreContext.getAccount();

		String secretKey = StringHasher.hashString(System.currentTimeMillis() + me.getEmail());
		privateToken.setAccountId(me.getId());
		privateToken.setSecretKey(secretKey);
		privateToken.setCreated(LocalDate.now());

		String jwt = this.tokenService.createPrivateTokenJWT(privateToken);

		privateToken.setSecretKey(StringHasher.hashString(privateToken.getSecretKey()));

		me.getMetadata().updateLastModification();
		me.getAuthentication().getPrivateTokens().add(privateToken);

		this.accountRepository.save(me);

		return new JWT(jwt);
	}

	public LeafAccount revokePrivateToken(String name) {
		LeafAccount me = this.coreContext.getAccount();
		PrivateToken tokenToRevoke = null;
		for (PrivateToken token : me.getAuthentication().getPrivateTokens()) {
			if (name.equals(token.getName())) {
				tokenToRevoke = token;
				break;
			}
		}
		me.getMetadata().updateLastModification();
		me.getAuthentication().getPrivateTokens().remove(tokenToRevoke);
		this.accountRepository.save(me);
		return me;
	}

	public void unsubscribeFromEmail(MailingUnsubscriptionAction mailingUnsubscriptionAction) {
		LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(mailingUnsubscriptionAction.getEmail());

		if (fetchedAccount == null) {
			throw new UnauthorizedException();
		}

		if (fetchedAccount.getCommunication() != null) {
			fetchedAccount.getCommunication().unsubscribe(mailingUnsubscriptionAction.getType());
			this.accountRepository.save(fetchedAccount);
		}
	}

	public LeafAccount sendEmailVerificationCode() {
		LeafAccount me = this.coreContext.getAccount();
		Map<String, Object> payload = new HashMap<>();
		payload.put("verificationCode", me.getAccountVerification().generateEmailVerificationCode());
		this.notificationService.emit(LeafNotification.of("LEAF_ACCOUNT_EMAIL_VERIFICATION", me.getId(), payload));
		return this.accountRepository.save(me);
	}

	public LeafAccount accountVerification(AccountVerification accountVerification) {
		LeafAccount me = this.coreContext.getAccount();
		if ("email".equalsIgnoreCase(accountVerification.getType())) {
			me.getAccountVerification().validateEmailVerificationCode(accountVerification.getCode());
		} else if ("mobile".equalsIgnoreCase(accountVerification.getType())) {
			me.getAccountVerification().validateMobileVerificationCode(accountVerification.getCode());
		}
		return this.accountRepository.save(me);
	}

	public LeafAccount updateProfile(LeafAccountProfile profile) {
		LeafAccount me = this.coreContext.getAccount();
		me.getProfile().updateWith(profile);
		return this.accountRepository.save(me);
	}
	
	public LeafAccount updateAccount(LeafAccount account) {
		LeafAccount existingAccount = this.accountRepository.findById(account.getId()).orElseThrow(() -> new NotFoundException());

		if (account.getEmail() != null) {
			existingAccount.setEmail(account.getEmail());
		}
		if ( account.getAuthentication() != null) {
			existingAccount.setAuthentication(account.getAuthentication());
		}
		if ( account.getProfile() != null) {
			existingAccount.setProfile(account.getProfile());
		}
		if ( account.getCommunication() != null) {
			existingAccount.setCommunication(account.getCommunication());
		}
		if ( account.getModules() != null) {
			existingAccount.setModules(account.getModules());
		}
		if ( account.getMetadata() != null) {
			existingAccount.setMetadata(account.getMetadata());
		}
		if ( account.getOrganizationIds() != null) {
			existingAccount.setOrganizationIds(account.getOrganizationIds());
		}
		if ( account.getAccountVerification() != null) {
			existingAccount.setAccountVerification(account.getAccountVerification());
		}
		existingAccount.setIsTemporary(account.isTemporary());
		existingAccount.setAdmin(account.isAdmin());

		return this.accountRepository.save(existingAccount);
	}
}