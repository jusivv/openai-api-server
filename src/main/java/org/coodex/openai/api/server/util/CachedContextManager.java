package org.coodex.openai.api.server.util;

import org.coodex.openai.api.server.data.entity.ChatMessageEntity;
import org.coodex.openai.api.server.data.repo.ChatMessageRepo;
import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ChatMessage;
import org.coodex.openai.api.server.model.ChatRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

public class CachedContextManager implements IContextManager {
    private ChatContext context;
    private ChatMessageRepo messageRepo;

    public CachedContextManager(ChatContext context, ChatMessageRepo messageRepo) {
        this.context = context;
        this.messageRepo = messageRepo;
    }

    @Override
    public void iterateChatMessage(Consumer<ChatMessage> consumer) {
        for (ChatMessage message : context.getMessages()) {
            consumer.accept(message);
        }
    }

    @Override
    @Transactional
    public void completion(ChatRole role, String message, int promptTokens, int completionTokens, int totalTokens) {
        ChatMessageEntity messageEntity = new ChatMessageEntity();
        messageEntity.setContextId(context.getContextId());
        messageEntity.setRole(role.toString());
        messageEntity.setMessage(message);
        messageEntity.setPromptTokens(promptTokens);
        messageEntity.setCompletionTokens(completionTokens);
        messageEntity.setTotalTokens(totalTokens);
        messageRepo.save(messageEntity);
        context.addMessage(role, message);
        context.addTokenUsage(promptTokens, completionTokens, totalTokens);
    }
}
