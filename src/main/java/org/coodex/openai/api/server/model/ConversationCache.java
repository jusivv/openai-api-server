package org.coodex.openai.api.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConversationCache {
    public static final String SESSION_NAME = "SESSION_" + ConversationCache.class.getName();
    private List<ChatContext> contexts;

    public ConversationCache() {
        contexts = new ArrayList<>();
    }

    public void iterate(Consumer<ChatContext> consumer) {
        contexts.forEach(c -> consumer.accept(c));
    }

    public ChatContext[] list() {
        return contexts.toArray(new ChatContext[0]);
    }

    public ChatContext get(String contextId) {
        for (ChatContext context : contexts) {
            if (context.getContextId().equals(contextId)) {
                return context;
            }
        }
        return null;
    }

    public boolean add(ChatContext context) {
        if (get(context.getContextId()) != null) {
            return false;
        }
        return contexts.add(context);
    }

    public ChatContext remove(String contextId) {
        int index = -1;
        for (ChatContext context : contexts) {
            index++;
            if (context.getContextId().equals(contextId)) {
                return contexts.remove(index);
            }
        }
        return null;
    }

    public void clear() {
        contexts.clear();
    }
}
