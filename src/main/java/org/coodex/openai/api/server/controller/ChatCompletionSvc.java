package org.coodex.openai.api.server.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.coodex.openai.api.server.domain.ConversationLoader;
import org.coodex.openai.api.server.data.entity.ChatContextEntity;
import org.coodex.openai.api.server.data.repo.ChatContextRepo;
import org.coodex.openai.api.server.data.repo.ChatMessageRepo;
import org.coodex.openai.api.server.domain.ChatCompletionInvoker;
import org.coodex.openai.api.server.model.*;
import org.coodex.openai.api.server.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/chat")
public class ChatCompletionSvc {
    private static Logger log = LoggerFactory.getLogger(ChatCompletionSvc.class);
    @Value("${openai.maxTokens}")
    private int maxTokens;
    @Value("${openai.chatTitle.prompt}")
    private String titlePrompt;
    private ChatCompletionInvoker invoker;
    private ConversationLoader conversationLoader;
    private ChatContextRepo contextRepo;
    private ChatMessageRepo messageRepo;
    private ExecutorService sseExecutor = Executors.newFixedThreadPool(4);
    @Autowired
    public ChatCompletionSvc(ChatCompletionInvoker invoker, ConversationLoader conversationLoader,
                             ChatContextRepo contextRepo, ChatMessageRepo messageRepo) {
        this.invoker = invoker;
        this.conversationLoader = conversationLoader;
        this.contextRepo = contextRepo;
        this.messageRepo = messageRepo;
    }

    @PostMapping("/ask")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    public ChatResp ask(HttpSession session, @RequestBody @Valid ChatReq req) {
        ConversationCache cache = conversationLoader.get(session);
        ChatContext context = cache.get(req.getConversationId());
        Assert.notNull(context, "context not found");
//        Assert.isTrue(context.getTotalTokens() < maxTokens, "tokens overflow");
        ChatResp resp = new ChatResp();
        resp.setConversationId(req.getConversationId());
        try {
            invoker.ask(req.getQuestion(), new CachedContextManager(context, messageRepo));
            resp.setAnswer(context.getLastAnswer());
            resp.setCompletionTokens(context.getCompletionTokens());
            resp.setTotalTokens(context.getTotalTokens());
            return resp;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private IChatCompletionSseCallback buildSseCallback(String conversationId, SseEmitter emitter) {
        Counter counter = new Counter();
        return (content, completionTokens, totalTokens, error) -> {
            try {
                if (error == null) {
                    if (content.equals("[DONE]")) {
                        emitter.send(content, MediaType.TEXT_PLAIN);
                        emitter.complete();
                    } else {
                        ChatSseResp resp = new ChatSseResp();
                        resp.setConversationId(conversationId);
                        resp.setAnswer(content);
                        resp.setCompletionTokens(completionTokens);
                        resp.setTotalTokens(totalTokens);
                        emitter.send(resp, MediaType.APPLICATION_JSON);
                    }
                } else {
                    emitter.completeWithError(new RuntimeException(error));
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                emitter.completeWithError(e);
            }
        };
    }

    @GetMapping("/sseAsk/{conversationId}/{content}")
    @RolesAllowed({ Const.ROLE_USER, Const.ROLE_ADMIN })
    public SseEmitter adkWithSse(HttpSession session,
                                 @PathVariable("conversationId") @NotBlank String conversationId,
                                 @PathVariable("content") @NotBlank String content) {
        ConversationCache cache = conversationLoader.get(session);
        ChatContext context = cache.get(conversationId);
        Assert.notNull(context, "context not found");
//        Assert.isTrue(context.getTotalTokens() < maxTokens, "tokens overflow");
        SseEmitter emitter = new SseEmitter();
        sseExecutor.execute(() -> {
            try {
                invoker.askWithSse(content, new CachedContextManager(context, messageRepo),
                        buildSseCallback(conversationId, emitter));
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        });
        return emitter;
    }

    @GetMapping("/getTitle/{conversationId}")
    @RolesAllowed({Const.ROLE_USER, Const.ROLE_ADMIN})
    public ChatResp conversationTitle(HttpSession session,
                                      @PathVariable("conversationId") @NotBlank String conversationId) {
        ConversationCache cache = conversationLoader.get(session);
        ChatContext context = cache.get(conversationId);
        Assert.notNull(context, "conversation not found");
        TransientContextManager transientContextManager = new TransientContextManager(context);
        try {
            invoker.ask(titlePrompt, transientContextManager);
            String title = transientContextManager.getAnswer();
            ChatContextEntity contextEntity = contextRepo.getReferenceById(conversationId);
            contextEntity.setContextTitle(title);
            contextRepo.save(contextEntity);
            context.setContextTitle(title);
            ChatResp resp = new ChatResp();
            resp.setConversationId(conversationId);
            resp.setAnswer(title);
            return resp;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
