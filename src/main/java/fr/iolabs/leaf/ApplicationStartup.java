package fr.iolabs.leaf;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.annotations.UseAccount;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${core.firstuser.enabled}")
    private Boolean firstUserFeatureEnabled;

    @Value("${core.firstuser.email}")
    private String firstUserEmail;

    @Value("${core.firstuser.password}")
    private String firstUserPassword;

    @Autowired
    private LeafAccountRepository<LeafAccount> accountRepository;

    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (this.firstUserFeatureEnabled) {
            LeafAccount existingFirstUser = this.accountRepository.findAccountByEmail(this.firstUserEmail);
            if (existingFirstUser == null) {
                LeafAccount firstUser = this.instanciate();
                firstUser.setEmail(this.firstUserEmail);
                firstUser.setPassword(this.firstUserPassword);
                firstUser.setAdmin(true);

                firstUser.hashPassword();

                this.accountRepository.save(firstUser);
            }
        }
        return;
    }

    public LeafAccount instanciate() {
        Class<? extends LeafAccount> accountClass = scanAndFindClass();
        LeafAccount account = null;
        try {
            account = accountClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return account;
    }

    private Class<? extends LeafAccount> scanAndFindClass() {
        Reflections reflections = new Reflections("");

        Set<Class<? extends LeafAccount>> subTypes = reflections.getSubTypesOf(LeafAccount.class);

        for (Class<? extends LeafAccount> subType : subTypes) {
            if (subType.isAnnotationPresent(UseAccount.class)) {
                return subType;
            }
        }

        return LeafAccount.class;
    }
}
