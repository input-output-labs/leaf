package fr.iolabs.leaf.common.emailing;


import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.common.emailing.models.LeafEmailingCategory;

@Service
public class LeafEmailingCustomCategorySeekingListener_Admin implements ApplicationListener<LeafEmailingCustomCategorySeekingEvent> {
	@Override
	public void onApplicationEvent(LeafEmailingCustomCategorySeekingEvent event) {
		LeafEmailingCategory adminCategory = new LeafEmailingCategory();
		adminCategory.setId("0");
		adminCategory.setName("Admins");
		event.categories().add(adminCategory);
	}
}
