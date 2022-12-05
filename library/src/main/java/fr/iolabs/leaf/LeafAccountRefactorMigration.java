package fr.iolabs.leaf;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;

public class LeafAccountRefactorMigration {
	public static LeafAccount migrate(LeafAccount account) {
		account.setAuthentication(new LeafAccountAuthentication());
		account.getAuthentication().setPassword(account.getPassword());
		account.getAuthentication().setResetPasswordKey(account.getResetPasswordKey());
		account.getAuthentication().setPrivateTokens(account.getPrivateTokens());
		account.getAuthentication().setHashedSessionTokens(account.getHashedSessionTokens());

		account.setProfile(new LeafAccountProfile());
		account.getProfile().setAvatarUrl(account.getAvatarUrl());
		account.getProfile().setUsername(account.getUsername());

		return account;
	}
}
