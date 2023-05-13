package org.coodex.openai.api.server.domain;

import org.coodex.openai.api.server.data.entity.LoginRetriesEntity;
import org.coodex.openai.api.server.data.repo.LoginRetriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class AccountHealthChecker {
    @Value("${app.login.retry.limit}")
    private int loginRetryLimit;
    @Value("${app.login.retry.freeze}")
    private int loginRetryFreeze;
    private LoginRetriesRepo loginRetriesRepo;

    @Autowired
    public AccountHealthChecker(LoginRetriesRepo loginRetriesRepo) {
        this.loginRetriesRepo = loginRetriesRepo;
    }

    public void authenticationFailed(String accountId) {
        LoginRetriesEntity retriesEntity = loginRetriesRepo.findById(accountId).orElse(new LoginRetriesEntity());
        if (!StringUtils.hasText(retriesEntity.getAccountId())) {
            retriesEntity.setAccountId(accountId);
        }
        retriesEntity.setContinuousFailureCount(retriesEntity.getContinuousFailureCount() + 1);
        retriesEntity.setLastFailureTime(System.currentTimeMillis());
        loginRetriesRepo.save(retriesEntity);
    }

    public void authenticationSuccessful(String accountId) {
        LoginRetriesEntity retriesEntity = loginRetriesRepo.findById(accountId).orElse(null);
        if (retriesEntity != null) {
            retriesEntity.setContinuousFailureCount(0);
            loginRetriesRepo.save(retriesEntity);
        }
    }

    public void check(String accountId) {
        LoginRetriesEntity retriesEntity = loginRetriesRepo.findById(accountId).orElse(null);
        if (retriesEntity != null && retriesEntity.getContinuousFailureCount() > loginRetryLimit) {
            Assert.isTrue(
                    retriesEntity.getLastFailureTime() + (loginRetryFreeze * 1000) < System.currentTimeMillis(),
                    "Your account has been temporarily suspended due to a security issue."
            );
            retriesEntity.setContinuousFailureCount(0);
            loginRetriesRepo.save(retriesEntity);
        }
    }
}
