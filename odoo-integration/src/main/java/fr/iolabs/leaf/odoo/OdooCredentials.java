package fr.iolabs.leaf.odoo;

import org.springframework.util.StringUtils;

public class OdooCredentials {

	private final String url;
	private final String db;
	private final String username;
	private final String password;

	public OdooCredentials(String url, String db, String username, String password) {
		if (!StringUtils.hasText(url)) {
			throw new OdooIntegrationException("Odoo URL is required");
		}
		if (!StringUtils.hasText(db)) {
			throw new OdooIntegrationException("Odoo database is required");
		}
		if (!StringUtils.hasText(username)) {
			throw new OdooIntegrationException("Odoo username is required");
		}
		if (!StringUtils.hasText(password)) {
			throw new OdooIntegrationException("Odoo password is required");
		}
		this.url = url.trim().replaceAll("/+$", "");
		this.db = db.trim();
		this.username = username.trim();
		this.password = password;
	}

	public static OdooCredentials fromModule(OdooModule module) {
		if (module == null || !module.isEnabled()) {
			throw new OdooIntegrationException("Odoo integration is not enabled");
		}
		return new OdooCredentials(module.getUrl(), module.getDb(), module.getUsername(), module.getPassword());
	}

	public String getUrl() {
		return url;
	}

	public String getDb() {
		return db;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
