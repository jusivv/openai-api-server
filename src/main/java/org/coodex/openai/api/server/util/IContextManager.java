package org.coodex.openai.api.server.util;

import org.coodex.openai.api.server.model.ChatMessage;
import org.coodex.openai.api.server.model.ChatRole;

import java.util.function.Consumer;

public interface IContextManager {
    void iterateChatMessage(Consumer<ChatMessage> consumer);

    void completion(ChatRole role, String message, int promptTokens, int completionTokens, int totalTokens);

    default void completion(ChatRole role, String message) {
        completion(role, message, 0, 0, 0);
    }
}
