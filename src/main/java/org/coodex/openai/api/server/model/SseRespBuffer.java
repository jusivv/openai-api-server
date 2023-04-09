package org.coodex.openai.api.server.model;

public class SseRespBuffer {

    private int completionTokens;
    private int totalTokens;
    private StringBuilder sb;
    private ChatRole role;

    public SseRespBuffer() {
        totalTokens = 0;
        sb = new StringBuilder();
    }

    public void addToken(String token) {
        if (token != null && !token.equals("")) {
            completionTokens ++;
            sb.append(token);
        }
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public String getContent() {
        return sb.toString();
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }
}
