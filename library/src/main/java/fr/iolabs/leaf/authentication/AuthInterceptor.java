package fr.iolabs.leaf.authentication;

import java.util.Optional;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.iolabs.leaf.common.TokenService;
import fr.iolabs.leaf.common.utils.StringHasher;
import fr.iolabs.leaf.eligibilities.LeafEligibilitiesService;
import fr.iolabs.leaf.eligibilities.LeafEligibility;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.annotations.LeafEligibilityCheck;
import fr.iolabs.leaf.common.annotations.MandatoryOrganization;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

public class AuthInterceptor extends HandlerInterceptorAdapter {

	private static final String AUTHORIZATION = "Authorization";
	private static final String ORGANIZATION = "Organization";

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafEligibilitiesService eligibilitiesService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		this.findConnectedAccount(request);

		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;

			boolean permitAll = method.hasMethodAnnotation(PermitAll.class);
			boolean adminOnly = method.hasMethodAnnotation(AdminOnly.class);
			boolean mandatoryOrganization = method.hasMethodAnnotation(MandatoryOrganization.class);
			boolean leafEligibilityCheck = method.hasMethodAnnotation(LeafEligibilityCheck.class);

			if (!permitAll) {
				boolean connected = this.coreContext.getAccount() != null;
				if (!connected) {
					throw new UnauthorizedException();
				}

				boolean isAdmin = this.coreContext.getAccount().isAdmin();
				if (adminOnly && !isAdmin) {
					throw new UnauthorizedException();
				}
				boolean isOrganizationMember = this.isOrganizationMember();
				if (mandatoryOrganization && !isOrganizationMember) {
					throw new UnauthorizedException();
				}
				if (leafEligibilityCheck) {
					String eligibilityKey = method.getMethodAnnotation(LeafEligibilityCheck.class).value();
					LeafEligibility eligibility = this.eligibilitiesService.getEligibility(eligibilityKey);
					if (eligibility == null || !eligibility.eligible) {
						throw new UnauthorizedException();
					}
				}
			}
		}
		return true;
	}

	private boolean isOrganizationMember() {
		LeafAccount account = this.coreContext.getAccount();
		LeafOrganization organization = this.coreContext.getOrganization();
		if (account != null && organization != null) {
			for(OrganizationMembership member : organization.getMembers()) {
				if (member.getAccountId().equals(account.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	private void findConnectedAccount(HttpServletRequest request) {
		String token = findToken(request, AUTHORIZATION);
		String organizationId = findToken(request, ORGANIZATION);

		if (token != null && !token.isEmpty()) {
			String accountId = tokenService.getAccountIdFromJWT(token);
			if (accountId == null) {
				throw new UnauthorizedException();
			}
			Optional<LeafAccount> account = this.accountRepository.findById(accountId);
			if (!account.isPresent()) {
				throw new UnauthorizedException();
			}

			if (tokenService.isSessionJWT(token)) {
				String hashedToken = StringHasher.hashString(token);
				boolean oneSessionTokenIsMatching = account.get().getAuthentication().getHashedSessionTokens().stream()
						.anyMatch(hashedToken::equals);

				if (!oneSessionTokenIsMatching) {
					throw new UnauthorizedException();
				}
			} else if (tokenService.isPrivateTokenJWT(token)) {
				PrivateToken privateToken = tokenService.getPrivateTokenFromPrivateTokenJWT(token);
				String hashSecretKey = StringHasher.hashString(privateToken.getSecretKey());
				boolean oneAccountTokenIsMatching = account.get().getAuthentication().getPrivateTokens().stream()
						.anyMatch(aToken -> hashSecretKey.equals(aToken.getSecretKey()));

				if (!oneAccountTokenIsMatching) {
					throw new UnauthorizedException();
				}
			}

			coreContext.setAccount(account.get());
		}
		if (organizationId != null && !organizationId.isEmpty()) {
			Optional<LeafOrganization> organization = this.organizationRepository.findById(organizationId);
			if (organization.isPresent()) {
				coreContext.setOrganization(organization.get());
			}
		}
	}

	private String findToken(HttpServletRequest request, String tokenName) {
		String token = request.getParameter(tokenName);

		if (token == null) {
			token = request.getHeader(tokenName);
		}

		if (token == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (tokenName.equals(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
		}
		return token;
	}
}