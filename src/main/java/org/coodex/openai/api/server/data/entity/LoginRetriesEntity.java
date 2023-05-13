package org.coodex.openai.api.server.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_login_retries")
public class LoginRetriesEntity {
    @Id
    @Column(length = 32)
    private String accountId;
    private int continuousFailureCount = 0;
    private long lastFailureTime = 0L;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getContinuousFailureCount() {
        return continuousFailureCount;
    }

    public void setContinuousFailureCount(int continuousFailureCount) {
        this.continuousFailureCount = continuousFailureCount;
    }

    public long getLastFailureTime() {
        return lastFailureTime;
    }

    public void setLastFailureTime(long lastFailureTime) {
        this.lastFailureTime = lastFailureTime;
    }
}
