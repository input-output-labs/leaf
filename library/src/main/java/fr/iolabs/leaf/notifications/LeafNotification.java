package fr.iolabs.leaf.notifications;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class LeafNotification {
	@Id
	private String id;
	private String code;
	private String targetAccountId;
	private LocalDateTime creationDate;
	private Map<String, Object> payload;
	private Map<LeafNotificationChannel, LeafNotificationChannelSendingStatus> channelSendingStatus;

	public static LeafNotification of(String code, String targetAccountId) {
		return LeafNotification.of(code, targetAccountId, new HashMap<>());
	}

	public static LeafNotification of(String code, String targetAccountId, Map<String, Object> payload) {
		LeafNotification notification = new LeafNotification();
		notification.code = code;
		notification.targetAccountId = targetAccountId;
		notification.payload = payload;
		notification.creationDate = LocalDateTime.now();
		notification.channelSendingStatus = new HashMap<LeafNotificationChannel, LeafNotificationChannelSendingStatus>();

		for (LeafNotificationChannel channel : LeafNotificationChannel.all()) {
			notification.channelSendingStatus.put(channel, LeafNotificationChannelSendingStatus.CREATED);
		}
		return notification;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTargetAccountId() {
		return targetAccountId;
	}

	public void setTargetAccountId(String targetAccountId) {
		this.targetAccountId = targetAccountId;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	public Map<LeafNotificationChannel, LeafNotificationChannelSendingStatus> getChannelSendingStatus() {
		return channelSendingStatus;
	}

	public void setChannelSendingStatus(
			Map<LeafNotificationChannel, LeafNotificationChannelSendingStatus> channelSendingStatus) {
		this.channelSendingStatus = channelSendingStatus;
	}
}
