package org.coodex.openai.api.server.model;

public class ChatCompletionSseResp extends ChatCompletionCommonResp {
    private String model;
    private ChatChoiceSse[] choices;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ChatChoiceSse[] getChoices() {
        return choices;
    }

    public void setChoices(ChatChoiceSse[] choices) {
        this.choices = choices;
    }
}
