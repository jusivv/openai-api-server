package org.coodex.openai.api.server.model;

import jakarta.validation.constraints.NotBlank;

public class LoginReq {
    @NotBlank
    private String name;
    @NotBlank
    private String pass;
    private boolean autoLogin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }
}
