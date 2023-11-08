package fr.iolabs.leaf.payment.models;

import java.util.Map;

import org.springframework.data.annotation.Id;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;

public class LeafPaymentTransaction {
	@Id
	private String id;
	private String customerId;
	private String checkoutSessionId;
	private Map<String, Object> modules;
	private ResourceMetadata metadata;
	private LeafPaymentTransactionStatusEnum status;

	public LeafPaymentTransaction(String customerId) {
		this.customerId = customerId;
		this.status = LeafPaymentTransactionStatusEnum.todo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Map<String, Object> getModules() {
		return modules;
	}

	public void setModules(Map<String, Object> modules) {
		this.modules = modules;
	}

	public ResourceMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}

	public String getCheckoutSessionId() {
		return checkoutSessionId;
	}

	public void setCheckoutSessionId(String checkoutSessionId) {
		this.checkoutSessionId = checkoutSessionId;
	}

	public LeafPaymentTransactionStatusEnum getStatus() {
		return status;
	}

	public void setStatus(LeafPaymentTransactionStatusEnum status) {
		this.status = status;
	}
}
