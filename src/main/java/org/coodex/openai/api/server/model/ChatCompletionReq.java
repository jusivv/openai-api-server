package org.coodex.openai.api.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ChatCompletionReq {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
    @JsonProperty("max_tokens")
    private int maxTokens;
    private boolean stream;

    public ChatCompletionReq() {
        messages = new ArrayList<>();
        stream = false;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void addMessage(ChatRole role, String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(content);
        messages.add(message);
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
