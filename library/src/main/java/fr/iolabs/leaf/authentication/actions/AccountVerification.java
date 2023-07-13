package fr.iolabs.leaf.authentication.actions;

public class AccountVerification {
	// email, mobile - only email supported for now
	private String type;
	// code
	private String code;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
