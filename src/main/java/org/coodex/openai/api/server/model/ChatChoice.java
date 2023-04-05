package org.coodex.openai.api.server.model;

public class ChatChoice extends Choice {
    private ChatMessage message;

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }
}
