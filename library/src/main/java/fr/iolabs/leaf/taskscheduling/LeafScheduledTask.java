package fr.iolabs.leaf.taskscheduling;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class LeafScheduledTask {
	@Id
	private String id;
	private String type;
	private ZonedDateTime executeAt;
	private ZonedDateTime lockedAt;
	private ZonedDateTime doneAt;
	private ZonedDateTime failedAt;
	private Map<String, Object> payload;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ZonedDateTime getExecuteAt() {
		return executeAt;
	}

	public void setExecuteAt(ZonedDateTime executeAt) {
		this.executeAt = executeAt;
	}

	public ZonedDateTime getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(ZonedDateTime lockedAt) {
		this.lockedAt = lockedAt;
	}

	public ZonedDateTime getDoneAt() {
		return doneAt;
	}

	public void setDoneAt(ZonedDateTime doneAt) {
		this.doneAt = doneAt;
	}

	public ZonedDateTime getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(ZonedDateTime failedAt) {
		this.failedAt = failedAt;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}
}
