package org.coodex.openai.api.server.model;

public class ChatMessageResp extends ChatMessage {
    private long createTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
