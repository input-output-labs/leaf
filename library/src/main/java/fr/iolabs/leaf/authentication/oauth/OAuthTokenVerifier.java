package fr.iolabs.leaf.authentication.oauth;

public interface OAuthTokenVerifier {
	/**
	 * @return the provider name this verifier handles (e.g., "google", "apple")
	 */
	String getProvider();

	/**
	 * Verifies the ID token and extracts user info.
	 *
	 * @param idToken the raw ID token from the provider SDK
	 * @return verified user info
	 * @throws fr.iolabs.leaf.common.errors.UnauthorizedException if verification fails
	 */
	OAuthUserInfo verify(String idToken);
}
