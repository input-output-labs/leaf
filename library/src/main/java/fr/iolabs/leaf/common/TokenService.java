package fr.iolabs.leaf.common;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.Claim;
import fr.iolabs.leaf.authentication.model.PrivateToken;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
public class TokenService {

	private static final String TOKEN_SECRET = "s4T2zOIWHMM1sxq";

	private static final String JWT_DATE_FORMAT = "dd-MM-yyyy";

	public String createSessionJWT(String userId) {
		Map<String, String> claims = new HashMap<>();
		claims.put("accountId", userId);
		claims.put("createdAt", new Date().toString());
		claims.put("type", "session");
		return this.createJWTFromClaims(claims);
	}

	public boolean isSessionJWT(String token) {
		return "session".equals(this.getTypeFromJWT(token));
	}

	public String createPrivateTokenJWT(PrivateToken privateToken) {
		Map<String, String> claims = new HashMap<>();
		claims.put("name", privateToken.getName());
		claims.put("accountId", privateToken.getAccountId());
		claims.put("expiration", privateToken.getExpiration() != null ? privateToken.getExpiration().format(DateTimeFormatter.ofPattern(JWT_DATE_FORMAT)) : null);
		claims.put("secret", privateToken.getSecretKey());
		claims.put("type", "privatetoken");
		return this.createJWTFromClaims(claims);
	}

	public boolean isPrivateTokenJWT(String token) {
		return "privatetoken".equals(this.getTypeFromJWT(token));
	}

	public PrivateToken getPrivateTokenFromPrivateTokenJWT(String token) {
		String name = this.getClaimsFromJWT(token).get("name");
		String account = this.getClaimsFromJWT(token).get("accountId");
		String expirationAsString = this.getClaimsFromJWT(token).get("expiration");
		String secret = this.getClaimsFromJWT(token).get("secret");

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
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();
			// log WRONG Encoding message
		} catch (JWTCreationException exception) {
			exception.printStackTrace();
			// log Token Signing Failed
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
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();
		} catch (JWTVerificationException exception) {
			exception.printStackTrace();
		}
		return claims;
	}

	public String getTypeFromJWT(String token) {
		return this.getClaimsFromJWT(token).get("type");
	}

	public String getAccountIdFromJWT(String token) {
		return this.getClaimsFromJWT(token).get("accountId");
	}
}
