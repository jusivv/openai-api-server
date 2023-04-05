package org.coodex.openai.api.server.model;

public class ChatSseResp extends ChatResp {
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
