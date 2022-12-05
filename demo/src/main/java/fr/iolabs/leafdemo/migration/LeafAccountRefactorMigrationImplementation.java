package fr.iolabs.leafdemo.migration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.LeafAccountRefactorMigration;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;

@Component
public class LeafAccountRefactorMigrationImplementation implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private LeafAccountRepository accountRepository;

	public void onApplicationEvent(final ApplicationReadyEvent event) {
		List<LeafAccount> allAccounts = this.accountRepository.findAll();
		System.out.println(allAccounts.size() + " accounts found");
		for (LeafAccount account : allAccounts) {
			if (this.shoudMigrate(account)) {
				LeafAccountRefactorMigration.migrate(account);
				System.out.println("Migrating account " + account.getId());
				this.accountRepository.save(account);
			}
		}
	}

	private boolean shoudMigrate(LeafAccount account) {
		return account.getAuthentication().getPassword() == null;
	}
}