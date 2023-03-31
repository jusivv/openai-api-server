package org.coodex.openai.api.server.controller;

import jakarta.validation.Valid;
import org.coodex.openai.api.server.domain.ChatCompletionInvoker;
import org.coodex.openai.api.server.domain.ConversationManager;
import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ChatReq;
import org.coodex.openai.api.server.model.ChatResp;
import org.coodex.openai.api.server.model.PromptReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class ChatCompletionSvc {
    private static Logger log = LoggerFactory.getLogger(ChatCompletionSvc.class);
    @Value("${openai.maxTokens}")
    private int maxTokens;
    private ChatCompletionInvoker invoker;
    private ConversationManager conversationManager;

    @Autowired
    public ChatCompletionSvc(ChatCompletionInvoker invoker, ConversationManager conversationManager) {
        this.invoker = invoker;
        this.conversationManager = conversationManager;
    }

    @PostMapping("/new")
    public ChatResp newChat(@RequestBody @Valid PromptReq req) {
        try {
            ChatContext context = invoker.begin(req.getQuestion());
            ChatResp resp = new ChatResp();
            resp.setConversationId(conversationManager.addConversation(context));
            resp.setAnswer(context.getLastAnswer());
            resp.setTotalTokens(context.getTotalTokens());
            return resp;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/ask")
    public ChatResp ask(@RequestBody @Valid ChatReq req) {
        ChatContext context = conversationManager.getContext(req.getConversationId());
        Assert.notNull(context, "context not found");
        Assert.isTrue(context.getTotalTokens() < maxTokens, "tokens overflow");
        ChatResp resp = new ChatResp();
        resp.setConversationId(req.getConversationId());
        try {
            invoker.ask(req.getQuestion(), context);
            resp.setAnswer(context.getLastAnswer());
            resp.setTotalTokens(context.getTotalTokens());
            return resp;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
