package org.coodex.openai.api.server.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpSession;
import org.coodex.openai.api.server.component.ConversationLoader;
import org.coodex.openai.api.server.data.entity.ChatContextEntity;
import org.coodex.openai.api.server.data.entity.ChatMessageEntity;
import org.coodex.openai.api.server.data.repo.ChatContextRepo;
import org.coodex.openai.api.server.data.repo.ChatMessageRepo;
import org.coodex.openai.api.server.model.*;
import org.coodex.openai.api.server.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/context")
public class ChatContextSvc {
    private ConversationLoader conversationLoader;
    private ChatContextRepo contextRepo;
    private ChatMessageRepo messageRepo;
    @Autowired
    public ChatContextSvc(ConversationLoader conversationLoader, ChatContextRepo contextRepo,
                          ChatMessageRepo messageRepo) {
        this.conversationLoader = conversationLoader;
        this.contextRepo = contextRepo;
        this.messageRepo = messageRepo;
    }

    @GetMapping("/list")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    public ContextListResp listContext(HttpSession session) {
        ConversationCache cache = conversationLoader.get(session);
        List<String> list = new ArrayList<>();
        cache.iterate(context -> list.add(context.getContextId()));
        ContextListResp resp = new ContextListResp();
        resp.setConversations(list.toArray(new String[0]));
        return resp;
    }

    @PostMapping("/create")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    @Transactional
    public ContextResp createChatContext(HttpSession session, @RequestBody PromptReq req) {
        LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
        ChatContextEntity contextEntity = new ChatContextEntity();
        contextEntity.setAccountId(loginAccount.getAccountId());
        contextEntity.setContextTitle("New conversation");
        contextRepo.save(contextEntity);
        ChatContext context = new ChatContext();
        context.setContextId(contextEntity.getContextId());
        context.setContextTitle(context.getContextTitle());
        if (StringUtils.hasText(req.getQuestion())) {
            ChatMessageEntity messageEntity = new ChatMessageEntity();
            messageEntity.setContextId(contextEntity.getContextId());
            messageEntity.setRole(ChatRole.SYSTEM.toString());
            messageEntity.setMessage(req.getQuestion());
            messageRepo.save(messageEntity);
            context.addMessage(ChatRole.SYSTEM, req.getQuestion());
        }
        ConversationCache cache = conversationLoader.get(session);
        cache.add(context);
        ContextResp resp = new ContextResp();
        resp.setConversationId(contextEntity.getContextId());
        return resp;
    }

    @GetMapping("/get/{id}")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    public ChatContext getChatContext(HttpSession session, @PathVariable("id") String conversationId) {
        ConversationCache cache = conversationLoader.get(session);
        ChatContext context = cache.get(conversationId);
        Assert.notNull(context, "context not found");
        return context;
    }

    @GetMapping("/delete/{id}")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    @Transactional
    public ChatContext deleteChat(HttpSession session, @PathVariable("id") String conversationId) {
        ConversationCache cache = conversationLoader.get(session);
        ChatContext context = cache.remove(conversationId);
        Assert.notNull(context, "context not found");
        messageRepo.deleteByContextId(conversationId);
        contextRepo.deleteById(conversationId);
        return context;
    }
}
