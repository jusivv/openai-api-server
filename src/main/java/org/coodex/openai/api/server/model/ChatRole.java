package org.coodex.openai.api.server.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");
    @JsonValue
    private final String role;

    private ChatRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
