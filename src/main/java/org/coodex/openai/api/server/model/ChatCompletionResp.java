package org.coodex.openai.api.server.model;

public class ChatCompletionResp extends ChatCompletionCommonResp {
    private ChatChoice[] choices;
    private TokenUsage usage;

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
