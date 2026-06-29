package fr.iolabs.leaf.odoo.sales;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleSearchCriteria {

	private ZonedDateTime createdAfter;
	private ZonedDateTime createdBefore;
	private List<String> states = new ArrayList<>();
	private Integer limit;

	public ZonedDateTime getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(ZonedDateTime createdAfter) {
		this.createdAfter = createdAfter;
	}

	public ZonedDateTime getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(ZonedDateTime createdBefore) {
		this.createdBefore = createdBefore;
	}

	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states != null ? new ArrayList<>(states) : new ArrayList<>();
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SaleSearchCriteria criteria = new SaleSearchCriteria();

		public Builder createdAfter(ZonedDateTime createdAfter) {
			criteria.setCreatedAfter(createdAfter);
			return this;
		}

		public Builder createdBefore(ZonedDateTime createdBefore) {
			criteria.setCreatedBefore(createdBefore);
			return this;
		}

		public Builder states(List<String> states) {
			criteria.setStates(states);
			return this;
		}

		public Builder limit(Integer limit) {
			criteria.setLimit(limit);
			return this;
		}

		public SaleSearchCriteria build() {
			return criteria;
		}
	}
}
