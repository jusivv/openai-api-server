package org.coodex.openai.api.server.data.repo;

import org.coodex.openai.api.server.data.entity.AccountRoleEntity;

import java.util.List;

public interface AccountRoleRepo extends CommonRepository<AccountRoleEntity> {
    List<AccountRoleEntity> findAllByAccountId(String accountId);

    int deleteByAccountId(String accountId);
}
