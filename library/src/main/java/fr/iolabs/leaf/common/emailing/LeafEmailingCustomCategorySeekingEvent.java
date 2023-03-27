package fr.iolabs.leaf.common.emailing;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.common.emailing.models.LeafEmailingCategory;

public class LeafEmailingCustomCategorySeekingEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	List<LeafEmailingCategory> categories;

	public LeafEmailingCustomCategorySeekingEvent(Object source, List<LeafEmailingCategory> categories) {
		super(source);
		this.categories = categories;
	}

	public List<LeafEmailingCategory> categories() {
		return categories;
	}
}