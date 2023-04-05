package org.coodex.openai.api.server.model;

public class ChatChoiceSse extends Choice {
    private ChatMessage delta;

    public ChatMessage getDelta() {
        return delta;
    }

    public void setDelta(ChatMessage delta) {
        this.delta = delta;
    }
}
