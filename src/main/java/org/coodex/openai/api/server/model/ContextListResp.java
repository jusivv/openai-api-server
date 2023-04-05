package org.coodex.openai.api.server.model;

public class ContextListResp {
    private String[] conversations;

    public String[] getConversations() {
        return conversations;
    }

    public void setConversations(String[] conversations) {
        this.conversations = conversations;
    }
}
