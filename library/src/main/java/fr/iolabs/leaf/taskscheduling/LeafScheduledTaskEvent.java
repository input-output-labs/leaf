package fr.iolabs.leaf.taskscheduling;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

public class LeafScheduledTaskEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private String type;
	private Map<String, Object> payload;
	private boolean done;

	public LeafScheduledTaskEvent(Object source, String type, Map<String, Object> payload) {
		super(source);
		this.type = type;
		this.payload = payload;
		this.done = false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}