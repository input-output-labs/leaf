package fr.iolabs.leaf.payment;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.plan.models.SelectedPlanModule;

@Service
public class PaymentMigrationService {
	private static final int PAGE_SIZE = 50;
	@Autowired
	private LeafAccountRepository accountRepository;
	@Autowired
	private LeafOrganizationRepository organizationRepository;
	@Autowired
	private LeafModuleService moduleService;
	
	@Autowired
    private MongoTemplate mongoTemplate;
    
	public void migrateAccounts() {
		this.migrate(LeafAccount.class, this::migratePaymentModules, this.accountRepository::saveAll);
	}
    
	public void migrateOrganizations() {
		this.migrate(LeafOrganization.class, this::migratePaymentModules, this.organizationRepository::saveAll);
	}
	
	public <T> void migrate(Class<T> clazz, Consumer<? super T> migrateEntity, Consumer<Iterable<T>> saveEntities) {
		int page = 0;
        List<T> entities = List.of();
        do {
        	System.out.print("[PaymentMigrationService] (" + clazz.getSimpleName() + ") PAGE_" + page);
        	try {
	            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
	            Query query = new Query()
	                    .addCriteria(
	                        new Criteria().andOperator(
	                            new Criteria().orOperator(
	                                    Criteria.where("modules.selectedplanmodule").exists(true),
	                                    Criteria.where("modules.paymentcustomermodule").exists(true)
	                            ),
	                            Criteria.where("modules.paymentmodule").exists(false)
	                        )
	                    )
	                    .with(pageable);
	            entities = mongoTemplate.find(query, clazz);
	            if (entities.size() > 0) {
	            	System.out.print(" | Found " + entities.size() + " entities | migrating...");
	                
	                // 🔄 Process entities (migrate / update / etc.)
	                entities.forEach(migrateEntity);
	            	System.out.print(" | saving...");
	                saveEntities.accept(entities);
	            	System.out.println(" | SAVED !");
	            } else {
	            	System.out.println(" | No entity found | STOPPING !");
	            }
        	} catch(Exception e) {
            	System.out.println(" | ERROR !");
        	}
            page++;
        } while (!entities.isEmpty());
	}
	
	private void migratePaymentModules(ILeafModular entity) {
		SelectedPlanModule selectedPlanModule = this.moduleService.get(SelectedPlanModule.class, entity);
		PaymentCustomerModule paymentCustomerModule = this.moduleService.get(PaymentCustomerModule.class, entity);
		PaymentModule paymentModule = this.moduleService.get(PaymentModule.class, entity);
		if (selectedPlanModule != null) {
			paymentModule.setSelectedPlan(selectedPlanModule.getSelectedPlan());
		}
		if (paymentCustomerModule != null) {
			paymentModule.setDefaultPaymentMethod(paymentCustomerModule.getDefaultPaymentMethod());
			paymentModule.setFreeTrialRemaining(paymentCustomerModule.getFreeTrialRemaining());
			paymentModule.setStripeCustomerId(paymentCustomerModule.getStripeId());
		}
	}
}
