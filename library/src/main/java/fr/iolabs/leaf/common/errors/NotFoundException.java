package fr.iolabs.leaf.common.errors;

public class NotFoundException extends WebException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        this("");
    }

    public NotFoundException(String message) {
        super(404, "Resource not found" + (message != null ? " - " + message : ""));
    }
}
