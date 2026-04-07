package fr.iolabs.leaf.authentication.oauth;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Component
public class GoogleTokenVerifier implements OAuthTokenVerifier {

	@Value("${leaf.oauth.google.clientId:}")
	private String clientId;

	private GoogleIdTokenVerifier verifier;

	@Override
	public String getProvider() {
		return "google";
	}

	@Override
	public OAuthUserInfo verify(String idToken) {
		if (clientId == null || clientId.isBlank()) {
			throw new UnauthorizedException("Google OAuth is not configured");
		}
		try {
			GoogleIdToken googleIdToken = getVerifier().verify(idToken);
			if (googleIdToken == null) {
				throw new UnauthorizedException("Invalid Google ID token");
			}

			GoogleIdToken.Payload payload = googleIdToken.getPayload();

			OAuthUserInfo info = new OAuthUserInfo();
			info.setProvider("google");
			info.setProviderUserId(payload.getSubject());
			info.setEmail(payload.getEmail()); // Warning from Google documentation: Don't use email address as an identifier because a Google Account can have multiple email addresses at different points in time. Always use the "sub" field as the identifier for the user.
			info.setName((String) payload.get("name"));
			info.setAvatarUrl((String) payload.get("picture"));
			return info;
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			throw new UnauthorizedException("Google token verification failed");
		}
	}

	private synchronized GoogleIdTokenVerifier getVerifier() {
		if (this.verifier == null) {
			this.verifier = new GoogleIdTokenVerifier.Builder(
					new NetHttpTransport(), GsonFactory.getDefaultInstance())
					.setAudience(Collections.singletonList(clientId))
					.build();
		}
		return this.verifier;
	}
}
