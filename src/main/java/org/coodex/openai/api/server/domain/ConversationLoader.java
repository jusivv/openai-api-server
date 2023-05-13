package org.coodex.openai.api.server.domain;

import jakarta.servlet.http.HttpSession;
import org.coodex.openai.api.server.data.entity.ChatContextEntity;
import org.coodex.openai.api.server.data.entity.ChatMessageEntity;
import org.coodex.openai.api.server.data.repo.ChatContextRepo;
import org.coodex.openai.api.server.data.repo.ChatMessageRepo;
import org.coodex.openai.api.server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ConversationLoader {
    private ChatContextRepo contextRepo;
    private ChatMessageRepo messageRepo;

    @Autowired
    public ConversationLoader(ChatContextRepo contextRepo, ChatMessageRepo messageRepo) {
        this.contextRepo = contextRepo;
        this.messageRepo = messageRepo;
    }

    public ConversationCache get(HttpSession session) {
        ConversationCache cache = (ConversationCache) session.getAttribute(ConversationCache.SESSION_NAME);
        if (cache == null) {
            LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
            Objects.requireNonNull(loginAccount);
            cache = new ConversationCache();
            for (ChatContextEntity contextEntity : contextRepo.findByAccountIdOrderByCreateTime(loginAccount.getAccountId())) {
                ChatContext context = new ChatContext();
                context.setContextId(contextEntity.getContextId());
                context.setContextTitle(contextEntity.getContextTitle());
                context.setCreateTime(contextEntity.getCreateTime());
                for (ChatMessageEntity messageEntity : messageRepo.findByContextIdOrderByCreateTime(context.getContextId())) {
                    context.addMessage(ChatRole.of(messageEntity.getRole()), messageEntity.getMessage(), messageEntity.getCreateTime());
                    context.addTokenUsage(messageEntity.getPromptTokens(), messageEntity.getCompletionTokens(),
                            messageEntity.getTotalTokens());
                }
                cache.add(context);
            }
            session.setAttribute(ConversationCache.SESSION_NAME, cache);
        }
        return cache;
    }
}
