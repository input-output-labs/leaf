package fr.iolabs.leaf.common.emailing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.data.domain.Pageable;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.emailing.models.LeafEmailingCategory;

public class LeafEmailingCustomCategoryAccountListingEvent extends ApplicationEvent {
	public static enum Action {
		COUNT, LIST;
	}

	private static final long serialVersionUID = -3508017274339153100L;

	private LeafEmailingCategory category;
	private List<LeafAccount> accounts;
	private Action action;
	private Pageable page;

	private long count;

	public LeafEmailingCustomCategoryAccountListingEvent(Object source, LeafEmailingCategory category, Action action) {
		this(source, category, action, null);
	}

	public LeafEmailingCustomCategoryAccountListingEvent(Object source, LeafEmailingCategory category, Action action, Pageable page) {
		super(source);
		this.accounts = new ArrayList<LeafAccount>();
		this.category = category;
		this.action = action;
		this.page = page;
	}

	public List<LeafAccount> accounts() {
		return accounts;
	}

	public Pageable page() {
		return page;
	}

	public LeafEmailingCategory category() {
		return category;
	}

	public Action action() {
		return action;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}