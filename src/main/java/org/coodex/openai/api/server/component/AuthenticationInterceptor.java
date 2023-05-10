package org.coodex.openai.api.server.component;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.coodex.openai.api.server.controller.AccountSvc;
import org.coodex.openai.api.server.data.entity.AccountEntity;
import org.coodex.openai.api.server.data.repo.AccountRepo;
import org.coodex.openai.api.server.model.LoginAccount;
import org.coodex.openai.api.server.util.Const;
import org.coodex.openai.api.server.util.EncryptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private static Base64.Decoder base64Decoder = Base64.getDecoder();

    private byte[] tokenKey;
    private AccountSvc accountSvc;
    private AccountRepo accountRepo;

    @Autowired
    public AuthenticationInterceptor(AccountSvc accountSvc, AccountRepo accountRepo,
                                     @Value("${app.token.key}") String tokenKey) {
        this.accountSvc = accountSvc;
        this.accountRepo = accountRepo;
        this.tokenKey = base64Decoder.decode(tokenKey);
    }

    private LoginAccount getLoginAccount(HttpServletRequest request) throws InvalidAlgorithmParameterException,
            IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IOException {
        LoginAccount loginAccount = (LoginAccount) request.getSession().getAttribute(LoginAccount.SESSION_NAME);
        if (loginAccount == null) {
            String token = request.getHeader(Const.TOKEN_HEADER_NAME);
            if (token != null) {
                String[] strings = token.split(Const.STRING_SPLITTER);
                if (strings.length == 2) {
                    byte[] plainText = EncryptHelper.decrypt(base64Decoder.decode(strings[0]), tokenKey,
                            base64Decoder.decode(strings[1]));
                    strings = new String(plainText, StandardCharsets.UTF_8).split(Const.STRING_SPLITTER);
                    if (strings.length == 2) {
                        String accountId = strings[0];
                        long expired = Long.parseLong(strings[1]);
                        log.debug("auto login accountId: {}, expired: {}", accountId, expired);
                        long now = System.currentTimeMillis();
                        if (now <= expired) {
                            AccountEntity accountEntity = accountRepo.findById(accountId)
                                    .orElseThrow(() -> new RuntimeException("fail to login"));
                            Assert.isTrue(expired == accountEntity.getTokenExpired(), "fail to login");
                            Assert.isTrue(!accountEntity.isLocked(), "fail to login");
                            loginAccount = accountSvc.buildSession(accountEntity, request.getSession());
                        }
                    }
                }
            }
        }
        return loginAccount;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            log.debug("request url: {}, class: {}, method: {}", request.getServletPath(),
                    method.getDeclaringClass().getName(), method.getName());
            LoginAccount loginAccount = getLoginAccount(request);
            RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
            if (rolesAllowed == null) {
                // no role needed
                return true;
            }
            if (loginAccount != null) {
                if (rolesAllowed.value().length == 0) {
                    // just login
                    return true;
                }
                Set<String> roleSet = new HashSet<>();
                roleSet.addAll(Arrays.asList(loginAccount.getRoles()));
                for (String role : rolesAllowed.value()) {
                    if (roleSet.contains(role)) {
                        // has role
                        return true;
                    }
                }
            }
        }
//        response.sendRedirect("/");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println(String.format("{\"code\": %d, \"message\": \"%s\"}",
                response.getStatus(), "forbidden"));
        return false;
    }
}
