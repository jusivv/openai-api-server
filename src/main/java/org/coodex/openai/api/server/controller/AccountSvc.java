package org.coodex.openai.api.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.coodex.openai.api.server.data.entity.AccountEntity;
import org.coodex.openai.api.server.data.repo.AccountRepo;
import org.coodex.openai.api.server.data.repo.AccountRoleRepo;
import org.coodex.openai.api.server.model.LoginAccount;
import org.coodex.openai.api.server.model.LoginReq;
import org.coodex.openai.api.server.model.LoginResp;
import org.coodex.openai.api.server.util.BCryptHelper;
import org.coodex.openai.api.server.util.Const;
import org.coodex.openai.api.server.util.EncryptHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("account")
public class AccountSvc {
    @Value("${app.token.expire-days}")
    private int tokenExpireDays;

    private byte[] tokenKey;

    private AccountRepo accountRepo;
    private AccountRoleRepo accountRoleRepo;
    private ObjectMapper objectMapper;

    private Base64.Encoder base64Encoder = Base64.getEncoder();
    private Base64.Decoder base64Decoder = Base64.getDecoder();

    @Autowired
    public AccountSvc(AccountRepo accountRepo, AccountRoleRepo accountRoleRepo, ObjectMapper objectMapper,
                      @Value("${app.token.key}") String tokenKey) {
        this.accountRepo = accountRepo;
        this.accountRoleRepo = accountRoleRepo;
        this.objectMapper = objectMapper;
        this.tokenKey = base64Decoder.decode(tokenKey);
    }

    public LoginAccount buildSession(AccountEntity accountEntity, HttpSession session) {
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setAccountId(accountEntity.getAccountId());
        loginAccount.setDisplayName(accountEntity.getDisplayName());
        final Set<String> roles = new HashSet<>();
        accountRoleRepo.findAllByAccountId(accountEntity.getAccountId()).iterator()
                .forEachRemaining(entity -> roles.add(entity.getRole()));
        loginAccount.setRoles(roles.toArray(new String[0]));
        session.setAttribute(LoginAccount.SESSION_NAME, loginAccount);
        return loginAccount;
    }

    @PostMapping("login")
    public LoginResp login(HttpSession session, @Valid @RequestBody LoginReq req) {
        AccountEntity accountEntity = accountRepo.getByAccountName(req.getName())
                .orElseThrow(() -> new RuntimeException("failed to login"));
        Assert.isTrue(!accountEntity.isLocked(), "your account is locked by admin");
        Assert.isTrue(BCryptHelper.verify(req.getPass(), accountEntity.getAccountPass()), "fail to login");
        buildSession(accountEntity, session);
        LoginResp resp = new LoginResp();
        resp.setSessionId(session.getId());
        if (req.isAutoLogin()) {
            accountEntity.setTokenExpired(System.currentTimeMillis() + tokenExpireDays * 24 * 3600 * 1000);
            try {
                byte[] iv = EncryptHelper.generateIV();
                resp.setToken(
                        base64Encoder.encodeToString(
                                EncryptHelper.encrypt(
                                        (accountEntity.getAccountId() + Const.STRING_SPLITTER + accountEntity.getTokenExpired()).getBytes(StandardCharsets.UTF_8),
                                        tokenKey, iv))
                                + Const.STRING_SPLITTER + base64Encoder.encodeToString(iv)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            accountEntity.setTokenExpired(0L);
        }
        accountRepo.save(accountEntity);
        return resp;
    }

    @GetMapping("logout")
    @RolesAllowed({})
    public String logout(HttpSession session) {
        LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
        if (loginAccount != null) {
            AccountEntity accountEntity = accountRepo.getReferenceById(loginAccount.getAccountId());
            accountEntity.setTokenExpired(0L);
            accountRepo.save(accountEntity);
            session.invalidate();
        }
        return "OK";
    }


}
