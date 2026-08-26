package fr.iolabs.leaf.organization;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrganizationSearchOrder {
	CREATION_DATE,
	NAME;

	@JsonCreator
	public static OrganizationSearchOrder fromValue(String value) {
		if (value == null || value.isBlank()) {
			return NAME;
		}
		String normalized = value.trim();
		for (OrganizationSearchOrder order : values()) {
			if (order.name().equalsIgnoreCase(normalized)) {
				return order;
			}
		}
		if ("creationdate".equalsIgnoreCase(normalized.replace("_", ""))) {
			return CREATION_DATE;
		}
		return NAME;
	}
}
