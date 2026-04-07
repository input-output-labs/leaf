package fr.iolabs.leaf.authentication.oauth;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.AccountRegistrationEvent;
import fr.iolabs.leaf.authentication.LeafAccountHelper;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.authentication.OAuthIdentity;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;

@Service
public class OAuthAccountService {

	private static final Logger logger = LoggerFactory.getLogger(OAuthAccountService.class);
	private static final int GENERATED_PASSWORD_LENGTH = 16;

	@Autowired
	private OAuthProviderRegistry providerRegistry;
	@Autowired
	private LeafAccountRepository accountRepository;
	@Autowired
	private LeafAccountService accountService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private LeafNotificationService notificationService;

	/**
	 * Main entry point for OAuth login/registration.
	 * Verifies the ID token, finds or creates an account, and returns a session token.
	 */
	public String authenticateWithOAuth(String provider, String idToken, String name) {
		// 1. Verify ID token with the appropriate provider
		OAuthTokenVerifier verifier = providerRegistry.getVerifier(provider);
		OAuthUserInfo userInfo = verifier.verify(idToken);

		// 2. Try to find existing account by OAuth identity
		LeafAccount account = accountRepository.findByOAuthIdentity(
				userInfo.getProvider(), userInfo.getProviderUserId());

		if (account != null) {
			logger.info("OAuth login: existing identity found for provider={} sub={}", provider, userInfo.getProviderUserId());
			String sessionToken = accountService.createSessionAndCookie(account);
			accountRepository.save(account);
			return sessionToken;
		}

		// 3. Try to find existing account by email (auto-link)
		if (!Strings.isBlank(userInfo.getEmail())) {
			account = accountRepository.findAccountByEmail(userInfo.getEmail().toLowerCase());
		}

		if (account != null) {
			logger.info("OAuth login: linking {} identity to existing account with email={}", provider, userInfo.getEmail());
			OAuthIdentity identity = new OAuthIdentity(
					userInfo.getProvider(),
					userInfo.getProviderUserId(),
					userInfo.getEmail());
			account.getAuthentication().addOAuthIdentityIfAbsent(identity);
			String sessionToken = accountService.createSessionAndCookie(account);
			accountRepository.save(account);
			return sessionToken;
		}

		// 4. Create a new account
		logger.info("OAuth login: creating new account for provider={} email={}", provider, userInfo.getEmail());
		account = createAccountFromOAuth(userInfo, name);

		applicationEventPublisher.publishEvent(new AccountRegistrationEvent(this, account));

		account = accountRepository.save(account);
		// We need to create the session after saving the account to ensure the account ID is generated and available for the session creation
		String sessionToken = accountService.createSessionAndCookie(account);
		account = accountRepository.save(account);

		this.notificationService.emit(
				LeafNotification.of("LEAF_ACCOUNT_REGISTRATION", account.getId(), account.toMap()));

		return sessionToken;
	}

	private LeafAccount createAccountFromOAuth(OAuthUserInfo userInfo, String name) {
		LeafAccount account = new LeafAccount();
		account.setMetadata(ResourceMetadata.create());

		if (!Strings.isBlank(userInfo.getEmail())) {
			account.setEmail(userInfo.getEmail().toLowerCase());
		}

		String firstname = userInfo.getFirstname();
		String lastname = userInfo.getLastname();

		// Determine display name
		String displayName = null;
		if (!Strings.isBlank(name)) {
			displayName = name;
		} else if (!Strings.isBlank(firstname) || !Strings.isBlank(lastname)) {
			displayName = (!Strings.isBlank(firstname) ? firstname : "") + (!Strings.isBlank(firstname) && !Strings.isBlank(lastname) ? " " : "") + (!Strings.isBlank(lastname) ? lastname : "");
		} else if (!Strings.isBlank(userInfo.getEmail())) {
			displayName = userInfo.getEmail();
		}
		if (displayName != null) {
			account.getProfile().setUsername(displayName);
		}
		if (!Strings.isBlank(firstname)) {
			account.getProfile().setFirstname(firstname);
		}
		if (!Strings.isBlank(lastname)) {
			account.getProfile().setLastname(lastname);
		}


		// Set avatar if provided (Google provides one)
		if (!Strings.isBlank(userInfo.getAvatarUrl())) {
			account.getProfile().setAvatarUrl(userInfo.getAvatarUrl());
		}

		// Generate a random password (account won't use password auth)
		LeafAccountAuthentication auth = account.getAuthentication();
		String randomPassword = LeafAccountHelper.generateComplexPassword(GENERATED_PASSWORD_LENGTH);
		auth.setPassword(randomPassword);
		auth.hashPassword();

		// Add OAuth identity
		OAuthIdentity identity = new OAuthIdentity(
				userInfo.getProvider(),
				userInfo.getProviderUserId(),
				userInfo.getEmail());
		auth.addOAuthIdentityIfAbsent(identity);

		return account;
	}
}
