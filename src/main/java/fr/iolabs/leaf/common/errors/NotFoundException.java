package fr.iolabs.leaf.common.errors;

public class NotFoundException extends WebException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super(404, "Resource not found");
    }
}
