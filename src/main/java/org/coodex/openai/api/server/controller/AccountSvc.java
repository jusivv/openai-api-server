package org.coodex.openai.api.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.coodex.openai.api.server.data.entity.AccountEntity;
import org.coodex.openai.api.server.data.entity.AccountRoleEntity;
import org.coodex.openai.api.server.data.entity.ChatContextEntity;
import org.coodex.openai.api.server.data.repo.AccountRepo;
import org.coodex.openai.api.server.data.repo.AccountRoleRepo;
import org.coodex.openai.api.server.data.repo.ChatContextRepo;
import org.coodex.openai.api.server.data.repo.ChatMessageRepo;
import org.coodex.openai.api.server.domain.AccountHealthChecker;
import org.coodex.openai.api.server.domain.HttpSessionHolder;
import org.coodex.openai.api.server.domain.PasswordGenerator;
import org.coodex.openai.api.server.model.*;
import org.coodex.openai.api.server.util.BCryptHelper;
import org.coodex.openai.api.server.util.Const;
import org.coodex.openai.api.server.util.EncryptHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("account")
public class AccountSvc {
    @Value("${app.token.expire-days}")
    private int tokenExpireDays;
    private byte[] tokenKey;
    private AccountRepo accountRepo;
    private AccountRoleRepo accountRoleRepo;
    private ChatContextRepo chatContextRepo;
    private ChatMessageRepo chatMessageRepo;
    private ObjectMapper objectMapper;
    private HttpSessionHolder sessionHolder;
    private AccountHealthChecker accountHealthChecker;
    private PasswordGenerator passwordGenerator;

    private Base64.Encoder base64Encoder = Base64.getEncoder();
    private Base64.Decoder base64Decoder = Base64.getDecoder();

    @Autowired
    public AccountSvc(AccountRepo accountRepo, AccountRoleRepo accountRoleRepo, ObjectMapper objectMapper,
                      ChatContextRepo chatContextRepo, ChatMessageRepo chatMessageRepo,
                      HttpSessionHolder sessionHolder, AccountHealthChecker accountHealthChecker,
                      PasswordGenerator passwordGenerator,
                      @Value("${app.token.key}") String tokenKey) {
        this.accountRepo = accountRepo;
        this.accountRoleRepo = accountRoleRepo;
        this.objectMapper = objectMapper;
        this.chatContextRepo = chatContextRepo;
        this.chatMessageRepo = chatMessageRepo;
        this.sessionHolder = sessionHolder;
        this.accountHealthChecker = accountHealthChecker;
        this.passwordGenerator = passwordGenerator;
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
        accountHealthChecker.check(accountEntity.getAccountId());
        if (!BCryptHelper.verify(req.getPass(), accountEntity.getAccountPass())) {
            accountHealthChecker.authenticationFailed(accountEntity.getAccountId());
            throw new RuntimeException("fail to login");
        }
        accountHealthChecker.authenticationSuccessful(accountEntity.getAccountId());
        sessionHolder.kickOut(accountEntity.getAccountId());
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
    public CommonResp logout(HttpSession session) {
        LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
        if (loginAccount != null) {
            AccountEntity accountEntity = accountRepo.getReferenceById(loginAccount.getAccountId());
            accountEntity.setTokenExpired(0L);
            accountRepo.save(accountEntity);
            session.invalidate();
        }
        return CommonResp.build(0, "OK");
    }

    @GetMapping("info")
    @RolesAllowed({})
    public LoginAccount info(HttpSession session) {
        return (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
    }

    @PostMapping("changePass")
    @RolesAllowed({})
    public CommonResp changePass(@Valid @RequestBody ChangePassReq req, HttpSession session) {
        LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
        Assert.notNull(loginAccount, "account not found");
        AccountEntity accountEntity = accountRepo.getReferenceById(loginAccount.getAccountId());
        if (BCryptHelper.verify(req.getCurrentPass(), accountEntity.getAccountPass())) {
            accountEntity.setAccountPass(BCryptHelper.hash(req.getNewPass()));
            accountRepo.save(accountEntity);
            return CommonResp.build(200, "Success");
        } else {
            throw new RuntimeException("authentication failed.");
        }
    }

    @PostMapping("add")
    @RolesAllowed({Const.ROLE_ADMIN})
    @Transactional
    public CommonResp addAccount(@Valid @RequestBody AccountAddReq req) {
        Assert.isNull(accountRepo.getByAccountName(req.getAccountName()).orElse(null),
                "Duplicate account name");
        AccountEntity accountEntity = new AccountEntity();
        BeanUtils.copyProperties(req, accountEntity,"admin");
        String pass = passwordGenerator.generate();
        accountEntity.setAccountPass(BCryptHelper.hash(pass));
        accountRepo.save(accountEntity);
        Set<AccountRoleEntity> roles = new HashSet<>();
        AccountRoleEntity roleEntity = new AccountRoleEntity();
        roleEntity.setAccountId(accountEntity.getAccountId());
        roleEntity.setRole(Const.ROLE_USER);
        roles.add(roleEntity);
        if (req.isAdmin()) {
            roleEntity = new AccountRoleEntity();
            roleEntity.setAccountId(accountEntity.getAccountId());
            roleEntity.setRole(Const.ROLE_ADMIN);
            roles.add(roleEntity);
        }
        accountRoleRepo.saveAll(roles);
        return CommonResp.build(200, pass);
    }

    @PostMapping("update")
    @RolesAllowed({Const.ROLE_ADMIN})
    @Transactional
    public CommonResp updateAccount(@Valid @RequestBody AccountEditReq req) {
        AccountEntity accountEntity = accountRepo.getReferenceById(req.getAccountId());
        if (!req.getAccountName().equals(accountEntity.getAccountName())) {
            Assert.isNull(accountRepo.getByAccountName(req.getAccountName()).orElse(null),
                    "Duplicate account name");
        }
        BeanUtils.copyProperties(req, accountEntity,"accountId", "admin");
        AccountRoleEntity roleEntity = accountRoleRepo.getByAccountIdAndRole(accountEntity.getAccountId(),
                Const.ROLE_ADMIN).orElse(null);
        if (!req.isAdmin()) {
            if (roleEntity != null) {
                accountRoleRepo.delete(roleEntity);
            }
        } else {
            if (roleEntity == null) {
                roleEntity = new AccountRoleEntity();
                roleEntity.setAccountId(accountEntity.getAccountId());
                roleEntity.setRole(Const.ROLE_ADMIN);
                accountRoleRepo.save(roleEntity);
            }
        }
        return CommonResp.build(200, "Success");
    }

    @GetMapping("resetPass/{accountId}")
    @RolesAllowed({Const.ROLE_ADMIN})
    public CommonResp resetPass(@PathVariable("accountId") @NotBlank String accountId) {
        AccountEntity accountEntity = accountRepo.getReferenceById(accountId);
        String pass = passwordGenerator.generate();
        accountEntity.setAccountPass(BCryptHelper.hash(pass));
        accountRepo.save(accountEntity);
        return CommonResp.build(200, pass);
    }

    @GetMapping("delete/{accountId}")
    @RolesAllowed({Const.ROLE_ADMIN})
    @Transactional
    public CommonResp deleteAccount(@PathVariable("accountId") @NotBlank String accountId) {
        // delete chat
        List<ChatContextEntity> contextList = chatContextRepo.findByAccountIdOrderByCreateTime(accountId);
        if (!contextList.isEmpty()) {
            Set<String> contextIds = new HashSet<>();
            contextList.forEach(contextEntity -> contextIds.add(contextEntity.getContextId()));
            chatMessageRepo.deleteByContextIdIn(contextIds);
            chatContextRepo.deleteAll(contextList);
        }
        // delete account
        accountRoleRepo.deleteByAccountId(accountId);
        accountRepo.deleteById(accountId);
        return CommonResp.build(200, "Success");
    }

    @GetMapping("list/{pageNo}")
    @RolesAllowed({Const.ROLE_ADMIN})
    public Page<AccountResp> listAccount(@PathVariable("pageNo") int pageNo) {
        PageRequest pageRequest = PageRequest.of(pageNo, 10, Sort.Direction.ASC, "accountName");
        Page<AccountEntity> page = accountRepo.findAll(pageRequest);
        return page.map(entity -> {
            AccountResp resp = new AccountResp();
            BeanUtils.copyProperties(entity, resp);
            resp.setAdmin(accountRoleRepo.getByAccountIdAndRole(entity.getAccountId(), Const.ROLE_ADMIN)
                    .orElse(null) != null);
            return resp;
        });
    }
}
