package org.coodex.openai.api.server.domain;

import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.util.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;

@Component
public class ConversationManager {
    private IDGenerator idGenerator;
    private Map<String, ChatContext> conversations;

    @Autowired
    public ConversationManager(IDGenerator idGenerator) {
        this.idGenerator = idGenerator;
        conversations = new Hashtable<>();
    }

    public String addConversation(ChatContext context) {
        String id = idGenerator.genId();
        conversations.put(id, context);
        return id;
    }

    public ChatContext getContext(String id) {
        return conversations.get(id);
    }

    public ChatContext removeContext(String id) {
        return conversations.remove(id);
    }

    public void clearContext() {
        conversations.clear();
    }

    public String[] getAllKeys() {
        return conversations.keySet().toArray(new String[0]);
    }
}
