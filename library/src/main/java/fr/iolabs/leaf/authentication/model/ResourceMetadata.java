package fr.iolabs.leaf.authentication.model;

import java.time.LocalDateTime;

public class ResourceMetadata {
	protected LocalDateTime creationDate;
	protected LocalDateTime lastModification;

	private ResourceMetadata() {
	}

	public static ResourceMetadata create() {
		ResourceMetadata newResourceMetadata = new ResourceMetadata();
		newResourceMetadata.creationDate = LocalDateTime.now();
		newResourceMetadata.lastModification = LocalDateTime.now();
		return newResourceMetadata;
	}

	public void updateLastModification() {
		this.lastModification = LocalDateTime.now();
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getLastModification() {
		return lastModification;
	}

	public void setLastModification(LocalDateTime lastModification) {
		this.lastModification = lastModification;
	}
}
