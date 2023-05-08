package fr.iolabs.leaf.notifications;

import java.util.EnumSet;
import java.util.Set;

public enum LeafNotificationChannel {
	UI, EMAIL, WS;

	public static Set<LeafNotificationChannel> all() {
		return EnumSet.allOf(LeafNotificationChannel.class);
	}
}
