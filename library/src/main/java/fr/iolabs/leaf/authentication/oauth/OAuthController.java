package fr.iolabs.leaf.authentication.oauth;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.authentication.actions.OAuthLoginAction;
import fr.iolabs.leaf.authentication.model.JWT;

@RestController
@RequestMapping("/api/account/oauth")
public class OAuthController {

	@Autowired
	private OAuthAccountService oauthAccountService;

	@CrossOrigin
	@PermitAll
	@PostMapping("/{provider}")
	public JWT oauthLogin(@PathVariable String provider, @RequestBody OAuthLoginAction action) {
		String token = oauthAccountService.authenticateWithOAuth(provider, action.getIdToken(), action.getName());
		return new JWT(token);
	}
}
