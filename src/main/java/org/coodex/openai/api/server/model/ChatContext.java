package org.coodex.openai.api.server.model;

import java.util.ArrayList;
import java.util.List;

public class ChatContext {
    private List<ChatMessage> messageList;
    private List<TokenUsage> tokenUsageList;
    private int completionTokens;
    private int totalTokens;

    public ChatContext() {
        messageList = new ArrayList<>();
        tokenUsageList = new ArrayList<>();
        completionTokens = 0;
        totalTokens = 0;
    }

    public void addMessage(ChatRole role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        messageList.add(chatMessage);
    }

    public void addTokenUsage(int promptTokens, int completionTokens, int totalTokens) {
        TokenUsage tokenUsage = new TokenUsage();
        tokenUsage.setPromptTokens(promptTokens);
        tokenUsage.setCompletionTokens(completionTokens);
        this.completionTokens += completionTokens;
        tokenUsage.setTotalTokens(totalTokens);
        this.totalTokens += totalTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public ChatMessage[] getMessages() {
        return messageList.toArray(new ChatMessage[0]);
    }

    public String getLastAnswer() {
        if (!messageList.isEmpty()) {
            ChatMessage message = messageList.get(messageList.size() - 1);
            if (message.getRole() == ChatRole.ASSISTANT) {
                return message.getContent();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return messageList.isEmpty();
    }
}
