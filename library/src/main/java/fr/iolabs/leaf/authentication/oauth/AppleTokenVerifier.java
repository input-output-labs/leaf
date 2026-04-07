package fr.iolabs.leaf.authentication.oauth;

import java.security.interfaces.RSAPublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Component
public class AppleTokenVerifier implements OAuthTokenVerifier {

	private static final Logger logger = LoggerFactory.getLogger(AppleTokenVerifier.class);

	private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";

	@Value("${leaf.oauth.apple.clientId:}")
	private String clientId;

	private JwkProvider jwkProvider;

	@Override
	public String getProvider() {
		return "apple";
	}

	@Override
	public OAuthUserInfo verify(String idToken) {
		if (clientId == null || clientId.isBlank()) {
			throw new UnauthorizedException("Apple OAuth is not configured");
		}
		try {
			DecodedJWT decodedJWT = JWT.decode(idToken);
			String kid = decodedJWT.getKeyId();

			JwkProvider provider = getJwkProvider();
			Jwk jwk = provider.get(kid);
			RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

			Algorithm algorithm = Algorithm.RSA256(publicKey, null);
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer(APPLE_ISSUER)
					.withAudience(clientId)
					.build();

			DecodedJWT verifiedJWT = verifier.verify(idToken);

			OAuthUserInfo info = new OAuthUserInfo();
			info.setProvider("apple");
			info.setProviderUserId(verifiedJWT.getSubject());
			info.setEmail(verifiedJWT.getClaim("email").asString());
			// Apple does not include name in the ID token;
			// it is passed separately by the frontend on first sign-in
			return info;
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Apple token verification failed", e);
			throw new UnauthorizedException("Apple token verification failed");
		}
	}

	private synchronized JwkProvider getJwkProvider() {
		if (this.jwkProvider == null) {
			try {
				this.jwkProvider = new JwkProviderBuilder(APPLE_JWKS_URL).build();
			} catch (Exception e) {
				throw new UnauthorizedException("Failed to initialize Apple JWKS provider");
			}
		}
		return this.jwkProvider;
	}
}
