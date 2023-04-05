package org.coodex.openai.api.server.controller;

import org.coodex.openai.api.server.domain.ConversationManager;
import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ContextListResp;
import org.coodex.openai.api.server.model.ContextResp;
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
    public ContextListResp listContext() {
        ContextListResp resp = new ContextListResp();
        resp.setConversations(conversationManager.getAllKeys());
        return resp;
    }

    @GetMapping("/create")
    public ContextResp createChatContext() {
        ContextResp resp = new ContextResp();
        ChatContext context = new ChatContext();
        resp.setConversationId(conversationManager.addConversation(context));
        return resp;
    }

    @GetMapping("/get/{id}")
    public ChatContext getChatContext(@PathVariable("id") String conversationId) {
        ChatContext context = conversationManager.getContext(conversationId);
        Assert.notNull(context, "context not found");
        return context;
    }

    @GetMapping("/delete/{id}")
    public ChatContext deleteChat(@PathVariable("id") String conversationId) {
        return conversationManager.removeContext(conversationId);
    }
}
