package fr.iolabs.leaf.odoo.quote;

import java.util.ArrayList;
import java.util.List;

public class OdooCreateQuoteRequest {

	private Integer opportunityId;
	private Integer partnerId;
	private List<OdooCreateQuoteLineRequest> lines = new ArrayList<>();

	public Integer getOpportunityId() {
		return opportunityId;
	}

	public void setOpportunityId(Integer opportunityId) {
		this.opportunityId = opportunityId;
	}

	public Integer getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}

	public List<OdooCreateQuoteLineRequest> getLines() {
		return lines;
	}

	public void setLines(List<OdooCreateQuoteLineRequest> lines) {
		this.lines = lines != null ? lines : new ArrayList<>();
	}
}
