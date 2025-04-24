package fr.iolabs.leaf.common;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.Claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import fr.iolabs.leaf.authentication.model.authentication.PrivateToken;

@Service
public class TokenService {
	
	private static final String SESSION = "session";

	private static final String PRIVATETOKEN2 = "privatetoken";

	private static final String SECRET = "secret";

	private static final String EXPIRATION = "expiration";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String CREATED_AT = "createdAt";

	private static final String ACCOUNT_ID = "accountId";

	private static Logger logger = LoggerFactory.getLogger(TokenService.class);

	private static final String TOKEN_SECRET = "s4T2zOIWHMM1sxq";

	private static final String JWT_DATE_FORMAT = "dd-MM-yyyy";

	public String createSessionJWT(String userId) {
		Map<String, String> claims = new HashMap<>();
		claims.put(ACCOUNT_ID, userId);
		claims.put(CREATED_AT, new Date().toString());
		claims.put(TYPE, SESSION);
		return this.createJWTFromClaims(claims);
	}

	public boolean isSessionJWT(String token) {
		return SESSION.equals(this.getTypeFromJWT(token));
	}

	public String createPrivateTokenJWT(PrivateToken privateToken) {
		Map<String, String> claims = new HashMap<>();
		claims.put(NAME, privateToken.getName());
		claims.put(ACCOUNT_ID, privateToken.getAccountId());
		claims.put(EXPIRATION, privateToken.getExpiration() != null ? privateToken.getExpiration().format(DateTimeFormatter.ofPattern(JWT_DATE_FORMAT)) : null);
		claims.put(SECRET, privateToken.getSecretKey());
		claims.put(TYPE, PRIVATETOKEN2);
		return this.createJWTFromClaims(claims);
	}

	public boolean isPrivateTokenJWT(String token) {
		return PRIVATETOKEN2.equals(this.getTypeFromJWT(token));
	}

	public PrivateToken getPrivateTokenFromPrivateTokenJWT(String token) {
		String name = this.getClaimsFromJWT(token).get(NAME);
		String account = this.getClaimsFromJWT(token).get(ACCOUNT_ID);
		String expirationAsString = this.getClaimsFromJWT(token).get(EXPIRATION);
		String secret = this.getClaimsFromJWT(token).get(SECRET);

		LocalDate expiration = expirationAsString != null ? LocalDate.parse(expirationAsString, DateTimeFormatter.ofPattern(JWT_DATE_FORMAT)) : null;

		if (account != null && secret != null) {
			return new PrivateToken(name, account, expiration, secret);
		}
		return null;
	}

	private String createJWTFromClaims(Map<String, String> claims) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
			JWTCreator.Builder token = JWT.create();
			for(Map.Entry<String, String> claim : claims.entrySet()) {
				token.withClaim(claim.getKey(), claim.getValue());
			}
			return token.sign(algorithm);
		} catch (JWTCreationException exception) {
			logger.error(exception.getMessage());
		}
		return null;
	}

	public Map<String, String> getClaimsFromJWT(String token) {
		Map<String, String> claims = new HashMap<>();
		try {
			Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(token);
			for(Map.Entry<String, Claim> claim: jwt.getClaims().entrySet()) {
				claims.put(claim.getKey(), claim.getValue().asString());
			}
		} catch (JWTVerificationException exception) {
			logger.error(exception.getMessage());
		}
		return claims;
	}

	public String getTypeFromJWT(String token) {
		return this.getClaimsFromJWT(token).get(TYPE);
	}

	public String getAccountIdFromJWT(String token) {
		return this.getClaimsFromJWT(token).get(ACCOUNT_ID);
	}
}
