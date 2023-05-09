package org.coodex.openai.api.server.data.entity;

import jakarta.persistence.*;
import org.coodex.openai.api.server.util.IDGenerator;

@Entity
@Table(name = "t_chat_message", indexes = {
        @Index(name = "i_cm_context", columnList = "contextId")
})
public class ChatMessageEntity {
    @Id
    @Column(length = 32)
    private String messageId = IDGenerator.genId();
    @Column(length = 32, nullable = false)
    private String contextId;
    @Column(length = 50, nullable = false)
    private String role;
    @Column(length = 4000, nullable = false)
    private String message;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long createTime = System.currentTimeMillis();

    public String getMessageId() {
        return messageId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }
}
