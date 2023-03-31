package org.coodex.openai.api.server.model;

import jakarta.validation.constraints.NotBlank;

public class PromptReq {
    @NotBlank
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
