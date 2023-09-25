package fr.iolabs.leaf.sponsoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.authentication.AccountRegistrationEvent;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.LeafModuleService;

@Component
public class Sponsoring_AccountCreationEventListener implements ApplicationListener<AccountRegistrationEvent> {

	@Autowired
	private LeafModuleService moduleService;

	@Override
	public void onApplicationEvent(AccountRegistrationEvent event) {
		LeafAccount account = event.account();
		Sponsoring sponsoring = this.moduleService.get(Sponsoring.class, account);
		sponsoring.setSponsorCode(account.getId());
	}

}
