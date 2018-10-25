package fr.iolabs.leaf.admin.whitelisting;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhitelistingService {

    @Value("${core.whitelisting.enabled}")
    private Boolean whitelistingFeatureEnabled;

    @Autowired
    private WhitelistingRepository authorizedEmailRepository;

    public boolean enabled() {
        return this.whitelistingFeatureEnabled;
    }

    public List<AuthorizedEmail> listAllAuthorizedEmails() {
        return this.authorizedEmailRepository.findAll();
    }

    public void addAuthorizedEmails(List<AuthorizedEmail> emails) {
        emails.forEach(email -> {
            if (!this.authorizedEmailRepository.existsById(email.email)) {
                this.authorizedEmailRepository.insert(email);
            }
        });
    }

    public void removeAuthorizedEmails(List<AuthorizedEmail> emails) {
        emails.forEach(email -> {
            if (this.authorizedEmailRepository.existsById(email.email)) {
                this.authorizedEmailRepository.delete(email);
            }
        });
    }

    public boolean isEmailAllowed(String email) {
        Optional<AuthorizedEmail> authorizedEmail = this.authorizedEmailRepository.findById(email);
        return !authorizedEmail.isPresent();
    }

}
