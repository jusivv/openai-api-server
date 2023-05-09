package org.coodex.openai.api.server.model;

public class LoginAccount {
    public static final String SESSION_NAME = "SESSION_" + LoginAccount.class.getName();

    private String accountId;
    private String displayName;
    private String[] roles;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }
}
