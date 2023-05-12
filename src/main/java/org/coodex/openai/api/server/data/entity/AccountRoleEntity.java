package org.coodex.openai.api.server.data.entity;

import jakarta.persistence.*;
import org.coodex.openai.api.server.util.IDGenerator;

@Entity
@Table(name = "t_account_role", indexes = {
        @Index(name = "i_ar_accountRole", columnList = "accountId, role")
})
public class AccountRoleEntity {
    @Id
    @Column(length = 32)
    private String recordId = IDGenerator.genId();

    @Column(length = 32, nullable = false)
    private String accountId;
    @Column(length = 50, nullable = false)
    private String role;

    public String getRecordId() {
        return recordId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
