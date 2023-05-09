package org.coodex.openai.api.server.data.entity;

import jakarta.persistence.*;
import org.coodex.openai.api.server.util.IDGenerator;

@Entity
@Table(name = "t_chat_context", indexes = {
        @Index(name = "i_cc_account", columnList = "accountId")
})
public class ChatContextEntity {
    @Id
    @Column(length = 32)
    private String contextId = IDGenerator.genId();
    @Column(length = 32, nullable = false)
    private String accountId;
    @Column(length = 500, nullable = false)
    private String contextTitle;
    private long createTime = System.currentTimeMillis();

    public String getContextId() {
        return contextId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setContextTitle(String contextTitle) {
        this.contextTitle = contextTitle;
    }
}
