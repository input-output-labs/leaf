package fr.iolabs.leaf.errors;

public class InternalServerErrorException extends WebException {
	private static final long serialVersionUID = 1L;

	public InternalServerErrorException() {
		super(500, "Internal Server Error");
	}
}
