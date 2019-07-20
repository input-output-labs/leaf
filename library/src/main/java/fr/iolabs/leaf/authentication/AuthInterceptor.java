package fr.iolabs.leaf.authentication;

import java.util.Optional;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.iolabs.leaf.authentication.model.PrivateToken;
import fr.iolabs.leaf.common.TokenService;
import fr.iolabs.leaf.common.utils.StringHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

public class AuthInterceptor<T extends LeafAccount> extends HandlerInterceptorAdapter {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private LeafAccountRepository<T> accountRepository;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		this.findConnectedAccount(request);

		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;

			boolean permitAll = method.hasMethodAnnotation(PermitAll.class);
			boolean adminOnly = method.hasMethodAnnotation(AdminOnly.class);

			if (!permitAll) {
				boolean connected = this.coreContext.getAccount() != null;
				if (!connected) {
					throw new UnauthorizedException();
				}

				boolean isAdmin = this.coreContext.getAccount().isAdmin();
				if (adminOnly && !isAdmin) {
					throw new UnauthorizedException();
				}
			}
		}
		return true;
	}

	private void findConnectedAccount(HttpServletRequest request) {
		String token = findToken(request);

		if (token != null && !token.isEmpty()) {
			String accountId = tokenService.getAccountIdFromJWT(token);
			if (accountId == null) {
				throw new UnauthorizedException();
			}
			Optional<T> account = this.accountRepository.findById(accountId);
			if (!account.isPresent()) {
				throw new UnauthorizedException();
			}

			if (tokenService.isSessionJWT(token)) {
				String hashedToken = StringHasher.hashString(token);
				boolean oneSessionTokenIsMatching = account.get().getHashedSessionTokens().stream()
						.anyMatch(aToken -> hashedToken.equals(aToken));

				if (!oneSessionTokenIsMatching) {
					throw new UnauthorizedException();
				}
			} else if (tokenService.isPrivateTokenJWT(token)) {
				PrivateToken privateToken = tokenService.getPrivateTokenFromPrivateTokenJWT(token);
				String hashSecretKey = StringHasher.hashString(privateToken.getSecretKey());
				boolean oneAccountTokenIsMatching = account.get().getPrivateTokens().stream()
						.anyMatch(aToken -> hashSecretKey.equals(aToken.getSecretKey()));

				if (!oneAccountTokenIsMatching) {
					throw new UnauthorizedException();
				}
			}

			coreContext.setAccount(account.get());
		}
	}

	private String findToken(HttpServletRequest request) {
		String token = request.getParameter("Authorization");

		if (token == null) {
			token = request.getHeader("Authorization");
		}

		if (token == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("Authorization".equals(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
		}
		return token;
	}
}