package fr.iolabs.leaf.authentication;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.method.HandlerMethod;

public class LeafCustomAuthorizationEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private String customAuthorization;
	private HandlerMethod method;
	private boolean accepted = false;

	public LeafCustomAuthorizationEvent(Object source, String customAuthorization, HandlerMethod method) {
		super(source);
		this.customAuthorization = customAuthorization;
		this.method = method;
	}

	public String getCustomAuthorization() {
		return customAuthorization;
	}

	public HandlerMethod getMethod() {
		return method;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
}