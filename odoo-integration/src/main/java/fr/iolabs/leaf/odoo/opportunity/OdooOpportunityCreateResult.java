package fr.iolabs.leaf.odoo.opportunity;

public class OdooOpportunityCreateResult {

	private boolean success;
	private Integer opportunityId;
	private String error;

	public static OdooOpportunityCreateResult success(int opportunityId) {
		OdooOpportunityCreateResult result = new OdooOpportunityCreateResult();
		result.success = true;
		result.opportunityId = opportunityId;
		return result;
	}

	public static OdooOpportunityCreateResult failure(String error) {
		OdooOpportunityCreateResult result = new OdooOpportunityCreateResult();
		result.success = false;
		result.error = error;
		return result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Integer getOpportunityId() {
		return opportunityId;
	}

	public void setOpportunityId(Integer opportunityId) {
		this.opportunityId = opportunityId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
