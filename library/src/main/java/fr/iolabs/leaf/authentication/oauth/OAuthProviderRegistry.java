package fr.iolabs.leaf.authentication.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.common.errors.BadRequestException;

@Component
public class OAuthProviderRegistry {

	@Autowired
	private List<OAuthTokenVerifier> verifiers;

	private Map<String, OAuthTokenVerifier> verifierMap;

	@PostConstruct
	public void init() {
		this.verifierMap = verifiers.stream()
				.collect(Collectors.toMap(OAuthTokenVerifier::getProvider, Function.identity()));
	}

	public OAuthTokenVerifier getVerifier(String provider) {
		OAuthTokenVerifier verifier = verifierMap.get(provider.toLowerCase());
		if (verifier == null) {
			throw new BadRequestException("Unsupported OAuth provider: " + provider);
		}
		return verifier;
	}
}
