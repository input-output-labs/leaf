package fr.iolabs.leaf.common.errors;

public class InternalServerErrorException extends WebException {
	private static final long serialVersionUID = 1L;

    public InternalServerErrorException() {
        this("");
    }

    public InternalServerErrorException(String message) {
        super(500, "Internal Server Error" + (message != null ? " - " + message : ""));
    }
}
