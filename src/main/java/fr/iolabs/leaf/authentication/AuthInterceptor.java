package fr.iolabs.leaf.authentication;

import java.util.Optional;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

public class AuthInterceptor<T extends LeafAccount> extends HandlerInterceptorAdapter {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LeafAccountRepository<T> accountRepository;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token != null && tokenService.isTokenValid(token)) {
            String accountId = tokenService.getUserIdFromToken(token);
            Optional<T> account = this.accountRepository.findById(accountId);

            if (!account.isPresent()) {
                throw new UnauthorizedException();
            }

            System.out.println(account.get().getClass().getName());

            coreContext.setAccount(account.get());
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;

            boolean permitAll = method.hasMethodAnnotation(PermitAll.class);
            boolean adminOnly = method.hasMethodAnnotation(AdminOnly.class);

            if (!permitAll) {
                boolean connected = this.coreContext.getAccount() != null;
                if (!connected) {
                    throw new UnauthorizedException();
                }

                boolean isAdmin = this.coreContext.getAccount().isAdmin();
                if (adminOnly && !isAdmin) {
                    throw new UnauthorizedException();
                }
            }
        }
        return true;
    }
}