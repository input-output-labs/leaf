package fr.iolabs.leaf.authentication.privacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.AccountVerification;
import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;

@Service
public class LeafPrivacyService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

    @Resource(name = "coreContext")
    private LeafContext coreContext;
	
	public List<LeafAccount> protectAccounts(List<LeafAccount> accounts) {
		return accounts.stream().map(this::protectAccount).collect(Collectors.toList());
	}

	public LeafAccount protectAccount(LeafAccount account) {
		LeafAccount result = new LeafAccount(account);
		result.setAuthentication(this.protectAuthentication(result.getAuthentication()));
		result.setAccountVerification(this.protectAccountVerification(result.getAccountVerification()));
		result.setPassword(null);
		result.setResetPasswordKey(null);
		result.setHashedSessionTokens(null);
		result.setPrivateTokens(result.getPrivateTokens().stream().map(this::protectPrivateToken).collect(Collectors.toSet()));
		result.setProfile(this.protectProfile(account.getId(), result.getProfile()));
		result.setCommunication(null);
		
		if (coreContext.getAccount() == null || !coreContext.getAccount().isAdmin()) {
			result.setGenericData(null);
		}
		
		this.applicationEventPublisher.publishEvent(new LeafAccountPrivacyEvent(this, account));
		
		return result;
	}

	private LeafAccountAuthentication protectAuthentication(LeafAccountAuthentication authentication) {
		LeafAccountAuthentication result = new LeafAccountAuthentication(authentication);
		result.setPassword(null);
		result.setResetPasswordKey(null);
		result.setHashedSessionTokens(null);
		result.setPrivateTokens(result.getPrivateTokens().stream().map(this::protectPrivateToken).collect(Collectors.toSet()));
		return result;
	}
	
	private AccountVerification protectAccountVerification(AccountVerification accountVerification) {
		AccountVerification result = new AccountVerification(accountVerification);
		result.setHashedEmailVerificationCode(null);
		result.setHashedMobileVerificationCode(null);
		return result;
	}

	private PrivateToken protectPrivateToken(PrivateToken privateToken) {
		PrivateToken result = new PrivateToken(privateToken);
		result.setSecretKey(null);
		return result;
	}

	public LeafAccountProfile protectProfile(String accountId, LeafAccountProfile profile) {
		LeafAccountProfile result = new LeafAccountProfile(profile);
		this.applicationEventPublisher.publishEvent(new LeafAccountProfilePrivacyEvent(this, accountId, result));
		return result;
	}
}
