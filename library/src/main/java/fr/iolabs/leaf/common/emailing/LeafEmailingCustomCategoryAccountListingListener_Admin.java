package fr.iolabs.leaf.common.emailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;

@Service
public class LeafEmailingCustomCategoryAccountListingListener_Admin
		implements ApplicationListener<LeafEmailingCustomCategoryAccountListingEvent> {

	@Autowired
	private LeafAccountRepository accountRepository;

	@Override
	public void onApplicationEvent(LeafEmailingCustomCategoryAccountListingEvent event) {
		switch (event.action()) {
		case COUNT:
			event.setCount(this.accountRepository.countByAdminTrue());
			break;
		case LIST:
			if (event.page() != null) {
				event.accounts().addAll(this.accountRepository.findByAdminTrue(event.page()));
			}
			break;
		default:
		}
	}
}
