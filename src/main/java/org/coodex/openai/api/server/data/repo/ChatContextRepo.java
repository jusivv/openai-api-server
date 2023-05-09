package org.coodex.openai.api.server.data.repo;

import org.coodex.openai.api.server.data.entity.ChatContextEntity;

import java.util.List;

public interface ChatContextRepo extends CommonRepository<ChatContextEntity> {
    List<ChatContextEntity> findByAccountIdOrderByCreateTime(String accountId);
}
