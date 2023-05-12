package org.coodex.openai.api.server.data.repo;

import org.coodex.openai.api.server.data.entity.AccountRoleEntity;

import java.util.List;
import java.util.Optional;

public interface AccountRoleRepo extends CommonRepository<AccountRoleEntity> {
    List<AccountRoleEntity> findAllByAccountId(String accountId);

    Optional<AccountRoleEntity> getByAccountIdAndRole(String accountId, String role);

    int deleteByAccountId(String accountId);

    int deleteByAccountIdAndRole(String accountId, String role);

}
