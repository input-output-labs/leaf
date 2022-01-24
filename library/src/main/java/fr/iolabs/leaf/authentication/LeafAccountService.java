package fr.iolabs.leaf.authentication;

import java.time.LocalDate;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.authentication.actions.LoginAction;
import fr.iolabs.leaf.authentication.actions.RegistrationAction;
import fr.iolabs.leaf.authentication.actions.ResetPasswordAction;
import fr.iolabs.leaf.authentication.actions.ChangePasswordAction;
import fr.iolabs.leaf.common.utils.StringHasher;
import fr.iolabs.leaf.common.TokenService;
import fr.iolabs.leaf.common.emailing.LeafAccountEmailing;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Service
public class LeafAccountService {

	private static Logger logger = LoggerFactory.getLogger(LeafAccountService.class);

	@Resource(name = "coreContext")
	private LeafContext coreContext;

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

	public LeafAccount me() {
		return this.coreContext.getAccount();
	}

	public String registerAndLogin(RegistrationAction userRegistration) {
		LeafAccount registeredAccount = this.register(userRegistration);
		logger.info("Account created with ID " + registeredAccount.getId());

		String sessionToken = this.createSession(registeredAccount);

		// Re-save account to register newly created session
		accountRepository.save(registeredAccount);

		return sessionToken;
	}

	public LeafAccount register(RegistrationAction userRegistration) {
		return this.register(userRegistration, false);
	}

	public LeafAccount register(RegistrationAction userRegistration, boolean force) {
		if (Strings.isBlank(userRegistration.getEmail()) || Strings.isBlank(userRegistration.getPassword())) {
			throw new BadRequestException();
		}

		LeafAccount existingAccount = this.accountRepository.findAccountByEmail(userRegistration.getEmail());
		if (existingAccount != null) {
			throw new BadRequestException();
		}

		if (!force && this.whitelistingService.enabled()
				&& this.whitelistingService.isEmailAllowed(userRegistration.getEmail())) {
			throw new UnauthorizedException();
		}

		LeafAccount instanciatedAccount = new LeafAccount();
		this.mergeRegistrationActionInLeafAccount(instanciatedAccount, userRegistration);

		this.applicationEventPublisher.publishEvent(new AccountRegistrationEvent(this, instanciatedAccount));

		instanciatedAccount.hashPassword();
		

		LeafAccount createdAccount = accountRepository.save(instanciatedAccount);

		this.accountEmailing.sendAccountCreationConfirmation(createdAccount);

		return createdAccount;
	}

	private void mergeRegistrationActionInLeafAccount(LeafAccount account, RegistrationAction action) {
		account.setEmail(action.getEmail());
		account.setPassword(action.getPassword());

		if (action.getUsername() != null) {
			account.setUsername(action.getUsername());
		}
		if (action.getAvatarUrl() != null) {
			account.setAvatarUrl(action.getAvatarUrl());
		}
		if (Strings.isBlank(account.getUsername())) {
			account.setUsername(account.getEmail());
		}
	}

	public String login(LoginAction accountLogin) {
		accountLogin.hashPassword();

		LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(accountLogin.email);
		if (fetchedAccount == null || !fetchedAccount.getPassword().equals(accountLogin.password)) {
			throw new UnauthorizedException();
		}

		String sessionToken = this.createSession(fetchedAccount);
		this.accountRepository.save(fetchedAccount);
		return sessionToken;
	}

	private String createSession(LeafAccount account) {
		String token = tokenService.createSessionJWT(account.getId());
		account.getHashedSessionTokens().add(StringHasher.hashString(token));

		Cookie cookie = new Cookie("Authorization", token);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		this.response.addCookie(cookie);
		return token;
	}

	public LeafAccount changePassword(ChangePasswordAction passwordChanger) {
		if (Strings.isBlank(passwordChanger.getOldPassword()) || Strings.isBlank(passwordChanger.getNewPassword())) {
			throw new BadRequestException();
		}

		String hashedOldPassword = StringHasher.hashString(passwordChanger.getOldPassword());

		LeafAccount me = this.coreContext.getAccount();
		if (!me.getPassword().equals(hashedOldPassword)) {
			throw new UnauthorizedException();
		}

		me.setPassword(passwordChanger.getNewPassword());
		me.hashPassword();

		return this.accountRepository.save(me);
	}

	public void sendResetPasswordKey(String email) {
		LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(email);
		if (fetchedAccount == null) {
			throw new UnauthorizedException();
		}

		fetchedAccount.generateResetPasswordKey();

		this.accountRepository.save(fetchedAccount);

		this.accountEmailing.sendResetPasswordKey(fetchedAccount, fetchedAccount.getResetPasswordKey());
	}

	public void resetPassword(ResetPasswordAction resetPasswordAction) {
		LeafAccount fetchedAccount = this.accountRepository.findAccountByResetPasswordKey(resetPasswordAction.getKey());

		if (fetchedAccount == null) {
			throw new UnauthorizedException();
		}

		fetchedAccount.setPassword(resetPasswordAction.getPassword());
		fetchedAccount.setResetPasswordKey(null);
		fetchedAccount.hashPassword();

		this.accountRepository.save(fetchedAccount);
	}

	public LeafAccount changeName(String newName) {
		if (Strings.isBlank(newName)) {
			throw new BadRequestException();
		}

		LeafAccount me = this.coreContext.getAccount();
		me.setUsername(newName);

		return this.accountRepository.save(me);
	}

	public LeafAccount changeAvatarUrl(String newAvatarUrl) {
		if (Strings.isBlank(newAvatarUrl)) {
			throw new BadRequestException();
		}

		LeafAccount me = this.coreContext.getAccount();
		me.setAvatarUrl(newAvatarUrl);

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

		me.getPrivateTokens().add(privateToken);

		this.accountRepository.save(me);

		return new JWT(jwt);
	}

	public LeafAccount revokePrivateToken(String name) {
		LeafAccount me = this.coreContext.getAccount();
		PrivateToken tokenToRevoke = null;
		for (PrivateToken token : me.getPrivateTokens()) {
			if (name.equals(token.getName())) {
				tokenToRevoke = token;
				break;
			}
		}
		me.getPrivateTokens().remove(tokenToRevoke);
		this.accountRepository.save(me);
		return me;
	}
}