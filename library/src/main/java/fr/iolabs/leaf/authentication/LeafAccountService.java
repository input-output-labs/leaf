package fr.iolabs.leaf.authentication;

import java.time.LocalDate;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.common.utils.StringHasher;
import fr.iolabs.leaf.common.TokenService;
import org.apache.logging.log4j.util.Strings;
import org.apache.tomcat.jni.Local;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.common.LeafEmailService;
import fr.iolabs.leaf.common.annotations.UseAccount;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Service
public class LeafAccountService {

    @Value("${leaf.myapp.package}")
    private String appPackage;

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private WhitelistingService whitelistingService;

    @Autowired
    private LeafAccountRepository<LeafAccount> accountRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private LeafEmailService emailService;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private HttpServletResponse response;

    public LeafAccount me() {
        return this.coreContext.getAccount();
    }

    public String register(LeafAccount account) {
        if (Strings.isBlank(account.getEmail()) || Strings.isBlank(account.getPassword())) {
            throw new BadRequestException();
        }

        LeafAccount existingAccount = this.accountRepository.findAccountByEmail(account.getEmail());
        if (existingAccount != null) {
            throw new BadRequestException();
        }

        if (this.whitelistingService.enabled() && this.whitelistingService.isEmailAllowed(account.getEmail())) {
            throw new UnauthorizedException();
        }

        LeafAccount instanciatedAccount = instanciate();
        instanciatedAccount.merge(account);

        instanciatedAccount.hashPassword();

        LeafAccount savedAccount = accountRepository.save(instanciatedAccount);
        return this.createSession(savedAccount);
    }

    private LeafAccount instanciate() {
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
        Reflections reflections = new Reflections(this.appPackage);

        Set<Class<? extends LeafAccount>> subTypes = reflections.getSubTypesOf(LeafAccount.class);

        for (Class<? extends LeafAccount> subType : subTypes) {
            if (subType.isAnnotationPresent(UseAccount.class)) {
                return subType;
            }
        }

        return LeafAccount.class;
    }

    public String login(LeafAccount account) {

        account.hashPassword();

        LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(account.getEmail());
        if (fetchedAccount == null || !fetchedAccount.getPassword().equals(account.getPassword())) {
            throw new UnauthorizedException();
        }
        return this.createSession(fetchedAccount);
    }

    private String createSession(LeafAccount account) {
        String token = tokenService.createSessionJWT(account.getId());
        this.response.addCookie(new Cookie("Authorization", token));
        return token;
    }

    public LeafAccount changePassword(PasswordChanger passwordChanger) {
        if (Strings.isBlank(passwordChanger.getOldPassword()) || Strings.isBlank(passwordChanger.getNewPassword())) {
            throw new BadRequestException();
        }

        String hashedOldPassword = StringHasher.hashString(passwordChanger.getOldPassword());

        LeafAccount me = this.coreContext.getAccount();
        if (!me.getPassword().equals(hashedOldPassword)) {
            throw new UnauthorizedException();
        }

        me.setPassword(passwordChanger.getNewPassword());
        me.hashPassword();

        return this.accountRepository.save(me);
    }

    public void sendResetPasswordKey(String email) {
        LeafAccount fetchedAccount = this.accountRepository.findAccountByEmail(email);
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
        LeafAccount fetchedAccount = this.accountRepository.findAccountByResetPasswordKey(passwordResetter.getKey());

        if (fetchedAccount == null) {
            throw new UnauthorizedException();
        }

        fetchedAccount.setPassword(passwordResetter.getPassword());
        fetchedAccount.setResetPasswordKey(null);
        fetchedAccount.hashPassword();

        this.accountRepository.save(fetchedAccount);
    }

    public LeafAccount changeName(String newName) {
        if (Strings.isBlank(newName)) {
            throw new BadRequestException();
        }

        LeafAccount me = this.coreContext.getAccount();
        me.setUsername(newName);

        return this.accountRepository.save(me);
    }

    public LeafAccount changeAvatarUrl(String newAvatarUrl) {
        if (Strings.isBlank(newAvatarUrl)) {
            throw new BadRequestException();
        }

        LeafAccount me = this.coreContext.getAccount();
        me.setAvatarUrl(newAvatarUrl);

        return this.accountRepository.save(me);
    }

    public JWT addPrivateToken(PrivateToken privateToken) {
        LeafAccount me = this.coreContext.getAccount();

        String secretKey = System.currentTimeMillis() + me.getEmail();
        privateToken.setAccountId(me.getId());
        privateToken.setSecretKey(secretKey);
        privateToken.setCreated(LocalDate.now());

        String jwt = this.tokenService.createPrivateTokenJWT(privateToken);

        privateToken.setSecretKey(StringHasher.hashString(privateToken.getSecretKey()));

        me.getPrivateTokens().add(privateToken);

        this.accountRepository.save(me);

        return new JWT(jwt);
    }

    public LeafAccount revokePrivateToken(String name) {
        LeafAccount me = this.coreContext.getAccount();
        PrivateToken tokenToRevoke = null;
        for(PrivateToken token : me.getPrivateTokens()) {
            if(name.equals(token.getName())) {
                tokenToRevoke = token;
                break;
            }
        }
        me.getPrivateTokens().remove(tokenToRevoke);
        this.accountRepository.save(me);
        return me;
    }
}