package org.coodex.openai.api.server.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.coodex.openai.api.server.util.IDGenerator;

@Entity
@Table(name = "t_account")
public class AccountEntity {
    @Id
    @Column(length = 32)
    private String accountId = IDGenerator.genId();

    @Column(length = 50, nullable = false, unique = true)
    private String accountName;

    @Column(length = 100, nullable = false)
    private String accountPass;

    @Column(length = 10)
    private String displayName;

    private boolean locked;

    private long tokenExpired;

    private long createTime = System.currentTimeMillis();

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountPass() {
        return accountPass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountPass(String accountPass) {
        this.accountPass = accountPass;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getTokenExpired() {
        return tokenExpired;
    }

    public void setTokenExpired(long tokenExpired) {
        this.tokenExpired = tokenExpired;
    }
}
