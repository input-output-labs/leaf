package fr.iolabs.leaf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.admin.LeafAdminService;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.actions.RegistrationAction;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${leaf.firstuser.enabled}")
    private Boolean firstUserFeatureEnabled;

    @Value("${leaf.firstuser.email}")
    private String firstUserEmail;

    @Value("${leaf.firstuser.password}")
    private String firstUserPassword;

    @Autowired
    private LeafAccountService accountService;
    
    @Autowired
    private LeafAdminService adminService;

    @Autowired
    private LeafAccountRepository accountRepository;

    @Autowired
    private LeafNotificationService notificationService;

    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (this.firstUserFeatureEnabled) {
            LeafAccount existingFirstUser = this.accountRepository.findAccountByEmail(this.firstUserEmail);
            if (existingFirstUser == null) {
            	RegistrationAction registrationAction = new RegistrationAction();
                registrationAction.setEmail(this.firstUserEmail);
                registrationAction.setUsername(this.firstUserEmail);
                registrationAction.setPassword(this.firstUserPassword);

                String accountId = this.accountService.register(registrationAction, true).getId();
                this.adminService.addAdmin(accountId);
            }
        }
    }
}
