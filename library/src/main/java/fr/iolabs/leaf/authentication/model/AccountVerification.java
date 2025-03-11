package fr.iolabs.leaf.authentication.model;

import java.util.Random;

import fr.iolabs.leaf.common.utils.StringHasher;

public class AccountVerification {
	private boolean emailVerified;
	private String hashedEmailVerificationCode;
	private boolean mobileVerified;
	private String hashedMobileVerificationCode;

	public AccountVerification() {
	}

	public AccountVerification(AccountVerification from) {
		this.emailVerified = from.emailVerified;
		this.hashedEmailVerificationCode = from.hashedEmailVerificationCode;
		this.mobileVerified = from.mobileVerified;
		this.hashedMobileVerificationCode = from.hashedMobileVerificationCode;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public boolean isMobileVerified() {
		return mobileVerified;
	}

	public void setMobileVerified(boolean mobileVerified) {
		this.mobileVerified = mobileVerified;
	}

	public String getHashedEmailVerificationCode() {
		return hashedEmailVerificationCode;
	}

	public void setHashedEmailVerificationCode(String hashedEmailVerificationCode) {
		this.hashedEmailVerificationCode = hashedEmailVerificationCode;
	}

	public String getHashedMobileVerificationCode() {
		return hashedMobileVerificationCode;
	}

	public void setHashedMobileVerificationCode(String hashedMobileVerificationCode) {
		this.hashedMobileVerificationCode = hashedMobileVerificationCode;
	}

	public void invalidateEmailVerification() {
		this.emailVerified = false;
		this.hashedEmailVerificationCode = null;
	}

	public void invalidateMobileVerification() {
		this.mobileVerified = false;
		this.hashedMobileVerificationCode = null;
	}

	public boolean validateEmailVerificationCode(String verificationCode) {
		String hashedVerificationCode = StringHasher.hashString(verificationCode);
		if (this.hashedEmailVerificationCode != null
				&& this.hashedEmailVerificationCode.equals(hashedVerificationCode)) {
			this.emailVerified = true;
			this.hashedEmailVerificationCode = null;
			return true;
		}
		return false;
	}

	public String generateMobileVerificationCode() {
		String verificationCode = this.generateRandomVerificationCode();
		this.hashedMobileVerificationCode = StringHasher.hashString(verificationCode);
		return verificationCode;
	}

	public boolean validateMobileVerificationCode(String verificationCode) {
		String hashedVerificationCode = StringHasher.hashString(verificationCode);
		if (this.hashedMobileVerificationCode != null
				&& this.hashedMobileVerificationCode.equals(hashedVerificationCode)) {
			this.mobileVerified = true;
			this.hashedMobileVerificationCode = null;
			return true;
		}
		return false;
	}

	public String generateEmailVerificationCode() {
		String verificationCode = this.generateRandomVerificationCode();
		this.hashedEmailVerificationCode = StringHasher.hashString(verificationCode);
		return verificationCode;
	}

	private String generateRandomVerificationCode() {
		int leftLimit = 48; // letter '0'
		int rightLimit = 57; // letter '9'
		int targetStringLength = 6;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		return generatedString;
	}
}
