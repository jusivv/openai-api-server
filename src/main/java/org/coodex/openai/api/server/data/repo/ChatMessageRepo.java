package org.coodex.openai.api.server.data.repo;

import org.coodex.openai.api.server.data.entity.ChatMessageEntity;

import java.util.List;

public interface ChatMessageRepo extends CommonRepository<ChatMessageEntity> {
    List<ChatMessageEntity> findByContextIdOrderByCreateTime(String contextId);

    int deleteByContextId(String contextId);
}
