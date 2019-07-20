package fr.iolabs.leaf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.model.LeafAccount;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${leaf.myapp.package}")
    private String appPackage;

    @Value("${leaf.firstuser.enabled}")
    private Boolean firstUserFeatureEnabled;

    @Value("${leaf.firstuser.email}")
    private String firstUserEmail;

    @Value("${leaf.firstuser.password}")
    private String firstUserPassword;

    @Autowired
    private LeafAccountService accountService;

    @Autowired
    private LeafAccountRepository<LeafAccount> accountRepository;

    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (this.firstUserFeatureEnabled) {
            LeafAccount existingFirstUser = this.accountRepository.findAccountByEmail(this.firstUserEmail);
            if (existingFirstUser == null) {
                LeafAccount firstUser = new LeafAccount();
                firstUser.setEmail(this.firstUserEmail);
                firstUser.setUsername(this.firstUserEmail);
                firstUser.setPassword(this.firstUserPassword);
                firstUser.setAdmin(true);

                this.accountService.register(firstUser);
            }
        }
        return;
    }
}
