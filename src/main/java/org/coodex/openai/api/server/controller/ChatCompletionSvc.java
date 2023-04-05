package org.coodex.openai.api.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.coodex.openai.api.server.domain.ChatCompletionInvoker;
import org.coodex.openai.api.server.domain.ConversationManager;
import org.coodex.openai.api.server.model.ChatContext;
import org.coodex.openai.api.server.model.ChatReq;
import org.coodex.openai.api.server.model.ChatResp;
import org.coodex.openai.api.server.model.ChatSseResp;
import org.coodex.openai.api.server.util.Counter;
import org.coodex.openai.api.server.util.IChatCompletionSseCallback;
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
    private ChatCompletionInvoker invoker;
    private ConversationManager conversationManager;

    private ExecutorService sseExecutor = Executors.newFixedThreadPool(4);

    @Autowired
    public ChatCompletionSvc(ChatCompletionInvoker invoker, ConversationManager conversationManager) {
        this.invoker = invoker;
        this.conversationManager = conversationManager;
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
    public SseEmitter adkWithSse(@PathVariable("conversationId") @NotBlank String conversationId,
                                 @PathVariable("content") @NotBlank String content) {
        ChatContext context = conversationManager.getContext(conversationId);
        Assert.notNull(context, "context not found");
        Assert.isTrue(context.getTotalTokens() < maxTokens, "tokens overflow");
        SseEmitter emitter = new SseEmitter();
        sseExecutor.execute(() -> {
            try {
                invoker.askWithSse(content, context,
                        buildSseCallback(conversationId, emitter));
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        });
        return emitter;
    }
}
