package org.coodex.openai.api.server.model;

public class ChatMessage {
    private ChatRole role;
    private String content;

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
