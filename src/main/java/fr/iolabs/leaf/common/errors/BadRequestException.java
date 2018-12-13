package fr.iolabs.leaf.common.errors;

public class BadRequestException extends WebException {
	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super(400, "Bad request");
	}
}
