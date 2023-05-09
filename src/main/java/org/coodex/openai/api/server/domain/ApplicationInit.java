package org.coodex.openai.api.server.domain;

import jakarta.annotation.PostConstruct;
import org.coodex.openai.api.server.data.entity.AccountEntity;
import org.coodex.openai.api.server.data.entity.AccountRoleEntity;
import org.coodex.openai.api.server.data.repo.AccountRepo;
import org.coodex.openai.api.server.data.repo.AccountRoleRepo;
import org.coodex.openai.api.server.util.BCryptHelper;
import org.coodex.openai.api.server.util.Const;
import org.coodex.openai.api.server.util.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplicationInit {
    private static Logger log = LoggerFactory.getLogger(ApplicationInit.class);
    @Value("${app.admin.user}")
    private String adminUser;
    @Value("${app.admin.pass}")
    private String adminPass;

    private AccountRepo accountRepo;
    private AccountRoleRepo accountRoleRepo;

    @Autowired
    public ApplicationInit(AccountRepo accountRepo, AccountRoleRepo accountRoleRepo) {
        this.accountRepo = accountRepo;
        this.accountRoleRepo = accountRoleRepo;
    }

    @PostConstruct
    @Transactional
    public void init() {
        AccountEntity accountEntity = accountRepo.getByAccountName(adminUser).orElse(null);
        if (accountEntity == null) {
            String pass = parsePass(adminPass);
            accountEntity = new AccountEntity();
            accountEntity.setAccountName(adminUser);
            accountEntity.setDisplayName(adminUser);
            accountEntity.setAccountPass(BCryptHelper.hash(pass));
            accountRepo.save(accountEntity);
            AccountRoleEntity role = new AccountRoleEntity();
            role.setAccountId(accountEntity.getAccountId());
            role.setRole(Const.ROLE_ADMIN);
            accountRoleRepo.save(role);
            log.info("create admin user \"{}\" with password \"{}\"", adminUser, pass);
        }
    }

    private String parsePass(String pass) {
        if (!pass.equals("")) {
            return pass;
        } else {
            return IDGenerator.genId();
        }
    }
}
