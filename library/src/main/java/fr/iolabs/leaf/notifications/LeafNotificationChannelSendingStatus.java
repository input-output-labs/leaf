package fr.iolabs.leaf.notifications;

public enum LeafNotificationChannelSendingStatus {
	CREATED,
	SKIP_NO_CONFIG,
	SKIP_PER_CONFIG,
	UI_SEEN,
	EMAIL_SENT,
	WS_SENT;
}
