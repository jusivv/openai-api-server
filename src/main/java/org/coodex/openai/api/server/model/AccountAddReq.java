package org.coodex.openai.api.server.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountAddReq {
    @NotBlank
    @Size(max = 50)
    private String accountName;
    @NotBlank
    @Size(max = 10)
    private String displayName;
    private boolean admin = false;
    private boolean locked = false;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
