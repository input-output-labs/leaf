package fr.iolabs.leaf.odoo;

public class OdooIntegrationException extends RuntimeException {

	public OdooIntegrationException(String message) {
		super(message);
	}

	public OdooIntegrationException(String message, Throwable cause) {
		super(message, cause);
	}
}
