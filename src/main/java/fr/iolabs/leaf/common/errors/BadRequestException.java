package fr.iolabs.leaf.common.errors;

public class BadRequestException extends WebException {
    private static final long serialVersionUID = 1L;

    public BadRequestException() {
        this("");
    }

    public BadRequestException(String message) {
        super(400, "Bad request" + message != null ? " - " + message : "");
    }
}
