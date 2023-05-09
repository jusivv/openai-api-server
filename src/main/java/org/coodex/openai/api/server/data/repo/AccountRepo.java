package org.coodex.openai.api.server.data.repo;

import org.coodex.openai.api.server.data.entity.AccountEntity;

import java.util.Optional;

public interface AccountRepo extends CommonRepository<AccountEntity> {
    Optional<AccountEntity> getByAccountName(String accountName);
}
