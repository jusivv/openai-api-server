package org.coodex.openai.api.server.model;

import jakarta.validation.constraints.NotBlank;

public class ChatReq extends PromptReq {
    @NotBlank
    private String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

}
