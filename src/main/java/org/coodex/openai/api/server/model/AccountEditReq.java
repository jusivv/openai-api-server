package org.coodex.openai.api.server.model;

import jakarta.validation.constraints.NotBlank;

public class AccountEditReq extends AccountAddReq {
    @NotBlank
    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
