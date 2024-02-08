package fr.iolabs.leaf.analytics.models;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class LeafAnalyticEvent {
	@Id
	private String id;
	private String sessionId;
	private String accountId;
	private String category;
	private String name;
	private ZonedDateTime creationDate;
	private Map<String, Object> payload;

	public boolean isValid() {
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object>  getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object>  payload) {
		this.payload = payload;
	}

	public ZonedDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}
}
