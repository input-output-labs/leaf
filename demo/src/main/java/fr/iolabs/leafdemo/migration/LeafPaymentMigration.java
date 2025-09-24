package fr.iolabs.leafdemo.migration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.LeafAccountRefactorMigration;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.payment.PaymentMigrationService;

@Component
public class LeafPaymentMigration /* implements ApplicationListener<ApplicationReadyEvent>*/ {

	@Autowired
	private PaymentMigrationService paymentMigrationService;

	public void onApplicationEvent(final ApplicationReadyEvent event) {
		this.paymentMigrationService.migrateAccounts();
		this.paymentMigrationService.migrateOrganizations();
	}
}