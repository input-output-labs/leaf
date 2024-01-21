package fr.iolabs.leaf.notifications;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties
@PropertySources({
    @PropertySource(value = "classpath:notifications.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "classpath:notifications-${spring.profiles.active}.yml", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class)
})
public class LeafNotificationConfig {
	private Map<String, Map<String, String>> notifications;

	public Map<String, Map<String, String>> getNotifications() {
		return notifications;
	}

	public void setNotifications(Map<String, Map<String, String>> notifications) {
		this.notifications = notifications;
	}

	public Map<String, String> getConfigByCode(String notificationCode) {
		return this.notifications != null ? this.notifications.get(notificationCode) : null;
	}
}
