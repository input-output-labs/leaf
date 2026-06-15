package fr.iolabs.leaf.odoo.config;

import fr.iolabs.leaf.notifications.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Configuration
@ConfigurationProperties(prefix = "odoo")
@PropertySources({
	@PropertySource(value = "classpath:odoo.yml", factory = YamlPropertySourceFactory.class),
	@PropertySource(
		value = "classpath:odoo-${spring.profiles.active}.yml",
		ignoreResourceNotFound = true,
		factory = YamlPropertySourceFactory.class
	)
})
public class LeafOdooConfig {

	private String url;
	private String db;
	private String username;
	private String password;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConfigured() {
		return (
			StringUtils.hasText(url) &&
			StringUtils.hasText(db) &&
			StringUtils.hasText(username) &&
			StringUtils.hasText(password)
		);
	}
}
