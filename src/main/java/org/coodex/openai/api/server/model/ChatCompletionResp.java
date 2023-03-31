package org.coodex.openai.api.server.model;

public class ChatCompletionResp {
    private String id;
    private String object;
    private long created;
    private ChatChoice[] choices;
    private TokenUsage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public ChatChoice[] getChoices() {
        return choices;
    }

    public void setChoices(ChatChoice[] choices) {
        this.choices = choices;
    }

    public TokenUsage getUsage() {
        return usage;
    }

    public void setUsage(TokenUsage usage) {
        this.usage = usage;
    }
}
