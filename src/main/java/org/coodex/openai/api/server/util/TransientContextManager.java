package org.coodex.openai.api.server.util;

import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ChatMessage;
import org.coodex.openai.api.server.model.ChatRole;

import java.util.function.Consumer;

public class TransientContextManager implements IContextManager {

    private ChatContext cache;
    private ChatContext context;

    public TransientContextManager(ChatContext cache) {
        this.cache = cache;
        this.context = new ChatContext();
    }

    @Override
    public void iterateChatMessage(Consumer<ChatMessage> consumer) {
        for (ChatMessage message : cache.getMessages()) {
            consumer.accept(message);
        }
        for (ChatMessage message : context.getMessages()) {
            consumer.accept(message);
        }
    }

    @Override
    public void completion(ChatRole role, String message, int promptTokens, int completionTokens, int totalTokens) {
        context.addMessage(role, message, System.currentTimeMillis());
        context.addTokenUsage(promptTokens, completionTokens, totalTokens);
    }

    public String getAnswer() {
        return context.getLastAnswer();
    }
}
