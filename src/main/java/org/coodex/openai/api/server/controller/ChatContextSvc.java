package org.coodex.openai.api.server.controller;

import org.coodex.openai.api.server.domain.ConversationManager;
import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/context")
public class ChatContextSvc {
    private ConversationManager conversationManager;

    @Autowired
    public ChatContextSvc(ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @GetMapping("/list")
    public String[] listContext() {
        return conversationManager.getAllKeys();
    }

    @GetMapping("/get/{id}")
    public ChatMessage[] getChatContext(@PathVariable("id") String conversationId) {
        ChatContext context = conversationManager.getContext(conversationId);
        Assert.notNull(context, "context not found");
        return context.getMessages();
    }

    @GetMapping("/delete/{id}")
    public String deleteChat(@PathVariable("id") String conversationId) {
        conversationManager.removeContext(conversationId);
        return conversationId;
    }
}
