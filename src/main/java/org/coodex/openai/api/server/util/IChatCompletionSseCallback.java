package org.coodex.openai.api.server.util;

@FunctionalInterface
public interface IChatCompletionSseCallback {
    void speak(String content, int completionTokens, int totalTokens, String error);
}
