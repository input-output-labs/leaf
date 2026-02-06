package fr.iolabs.leaf.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.method.HandlerMethod;

public class LeafCustomAuthorizationEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private String customAuthorization;
	private HttpServletRequest request;
	private HandlerMethod method;
	private String error = null;

	public LeafCustomAuthorizationEvent(Object source, String customAuthorization, HttpServletRequest request, HandlerMethod method) {
		super(source);
		this.request = request;
		this.customAuthorization = customAuthorization;
		this.method = method;
	}

	public String getCustomAuthorization() {
		return customAuthorization;
	}

	public HandlerMethod getMethod() {
		return method;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
}