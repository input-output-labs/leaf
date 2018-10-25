package fr.iolabs.leaf.authentication;

import javax.annotation.Resource;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.EmailService;
import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.PasswordChanger;
import fr.iolabs.leaf.authentication.model.PasswordResetter;
import fr.iolabs.leaf.errors.BadRequestException;
import fr.iolabs.leaf.errors.UnauthorizedException;

@Service
public class LeafAccountService<T extends LeafAccount> {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private WhitelistingService whitelistingService;

    @Autowired
    private LeafAccountRepository<T> accountRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TemplateEngine templateEngine;

    public String register(T account) {
        if (Strings.isBlank(account.getEmail()) || Strings.isBlank(account.getPassword())) {
            throw new BadRequestException();
        }

        T existingAccount = this.accountRepository.findAccountByEmail(account.getEmail());
        if (existingAccount != null) {
            throw new BadRequestException();
        }

        if (this.whitelistingService.enabled() && this.whitelistingService.isEmailAllowed(account.getEmail())) {
            throw new UnauthorizedException();
        }

        account.hashPassword();

        T savedAccount = accountRepository.save(account);
        return tokenService.createToken(savedAccount.getId());
    }

    public String login(T account) {

        account.hashPassword();

        T fetchedAccount = this.accountRepository.findAccountByEmail(account.getEmail());
        if (fetchedAccount == null || !fetchedAccount.getPassword().equals(account.getPassword())) {
            throw new UnauthorizedException();
        }
        return tokenService.createToken(fetchedAccount.getId());
    }

    public T changePassword(PasswordChanger passwordChanger) {
        if (Strings.isBlank(passwordChanger.getOldPassword()) || Strings.isBlank(passwordChanger.getNewPassword())) {
            throw new BadRequestException();
        }

        String hashedOldPassword = PasswordHasher.hashPassword(passwordChanger.getOldPassword());

        T me = this.coreContext.getAccount();
        if (!me.getPassword().equals(hashedOldPassword)) {
            throw new UnauthorizedException();
        }

        me.setPassword(passwordChanger.getNewPassword());
        me.hashPassword();

        return this.accountRepository.save(me);
    }

    public void sendResetPasswordKey(String email) {
        T fetchedAccount = this.accountRepository.findAccountByEmail(email);
        if (fetchedAccount == null) {
            throw new UnauthorizedException();
        }

        fetchedAccount.generateResetPasswordKey();

        this.accountRepository.save(fetchedAccount);

        Context context = new Context();
        context.setVariable("resetPasswordKey", fetchedAccount.getResetPasswordKey());
        String html = templateEngine.process("emailSendPasswordResetKey", context);

        this.emailService.sendEmail(fetchedAccount.getEmail(),
                "Votre clé de re-initialisation de mot de passe.",
                "Clé de re-initialisation : " + fetchedAccount.getResetPasswordKey(),
                html);
    }

    public void resetPassword(PasswordResetter passwordResetter) {
        T fetchedAccount = this.accountRepository.findAccountByResetPasswordKey(passwordResetter.getKey());

        if (fetchedAccount == null) {
            throw new UnauthorizedException();
        }

        fetchedAccount.setPassword(passwordResetter.getPassword());
        fetchedAccount.setResetPasswordKey(null);
        fetchedAccount.hashPassword();

        this.accountRepository.save(fetchedAccount);
    }
}