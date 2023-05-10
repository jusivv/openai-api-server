package org.coodex.openai.api.server.model;

import java.util.ArrayList;
import java.util.List;

public class ChatContext {
    private String contextId;
    private String contextTitle;
    private List<ChatMessageResp> messageList;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long createTime;

    public ChatContext() {
        messageList = new ArrayList<>();
        promptTokens = 0;
        completionTokens = 0;
        totalTokens = 0;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public void setContextTitle(String contextTitle) {
        this.contextTitle = contextTitle;
    }

    public void addMessage(ChatRole role, String content, long createTime) {
        ChatMessageResp chatMessage = new ChatMessageResp();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessage.setCreateTime(createTime);
        messageList.add(chatMessage);
    }

    public void addTokenUsage(int promptTokens, int completionTokens, int totalTokens) {
        this.promptTokens += promptTokens;
        this.completionTokens += completionTokens;
        this.totalTokens += totalTokens;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public ChatMessage[] getMessages() {
        return messageList.toArray(new ChatMessageResp[0]);
    }

    public String getLastAnswer() {
        if (!messageList.isEmpty()) {
            ChatMessageResp message = messageList.get(messageList.size() - 1);
            if (message.getRole() == ChatRole.ASSISTANT) {
                return message.getContent();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return messageList.isEmpty();
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
