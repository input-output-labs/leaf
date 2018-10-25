package fr.iolabs.leaf.errors;

public class NotFoundException extends WebException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super(404, "Resource not found");
    }
}
