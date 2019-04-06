package fr.iolabs.leaf.common.errors;

public class UnauthorizedException extends WebException {
	private static final long serialVersionUID = 4265202783057871003L;

	public UnauthorizedException() {
		super(401, "Unauthorized");
	}
}
