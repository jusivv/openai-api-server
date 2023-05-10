package org.coodex.openai.api.server.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.coodex.openai.api.server.model.*;
import org.coodex.openai.api.server.util.IChatCompletionSseCallback;
import org.coodex.openai.api.server.util.IContextManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Component
public class ChatCompletionInvoker {
    private static Logger log = LoggerFactory.getLogger(ChatCompletionInvoker.class);
    private static final String PATH = "chat/completions";
    private OkHttpClient httpClient;
    private HttpUrl httpUrl;
    @Value("${openai.apiKey}")
    private String apiKey;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.temperature}")
    private double temperature;
    @Value("${openai.maxTokens}")
    private int maxTokens;
    @Value("${openai.reduceToken:true}")
    private boolean reduceToken;
    private ObjectMapper objectMapper;

    @Autowired
    public ChatCompletionInvoker(
        @Value("${openai.apiServer.schema}") String serverSchema,
        @Value("${openai.apiServer.host}") String serverHost,
        @Value("${openai.apiServer.port}") int serverPort,
        @Value("${openai.apiServer.basePath}") String serverBasePath,
        @Value("${openai.proxy.type}") String proxyType,
        @Value("${openai.proxy.host}") String proxyHost,
        @Value("${openai.proxy.port}") String proxyPort,
        @Value("${openai.proxy.user}") String proxyUser,
        @Value("${openai.proxy.pass}") String proxyPass,
        @Value("${openai.timeout.connect}") int connectTimeout,
        @Value("${openai.timeout.read}") int readTimeout,
        @Value("${openai.timeout.write}") int writeTimeout,
        ObjectMapper objectMapper
    ) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS);
        if (StringUtils.hasText(proxyType) && StringUtils.hasText(proxyHost)
            && StringUtils.hasText(proxyPort)) {
            Proxy.Type type = null;
            try {
                type = Proxy.Type.valueOf(proxyType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
            if (type != null) {
                try {
                    int port = Integer.parseInt(StringUtils.trimAllWhitespace(proxyPort));
                    builder.proxy(new Proxy(type, new InetSocketAddress(proxyHost, port)));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Illegal proxy port: " + proxyPort);
                }
            } else {
                throw new RuntimeException("Illegal proxy type: " + proxyType);
            }
            if (StringUtils.hasText(proxyUser) || StringUtils.hasText(proxyPass)) {
                Authenticator proxyAuthenticator = new Authenticator() {
                    @Nullable
                    @Override
                    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
                        String credential = Credentials.basic(
                            proxyUser != null ? proxyUser : "",
                            proxyPass != null ? proxyPass : ""
                        );
                        return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                    }
                };
                builder.proxyAuthenticator(proxyAuthenticator);
            }
            log.info("Use proxy to access {}", serverHost);
        } else {
            log.info(
                "Please confirm that you can access {} directly, otherwise, you can specify both type, host, and port of the proxy server to use the proxy");
        } 
        httpClient = builder.build();
        httpUrl = new HttpUrl.Builder().scheme(serverSchema).host(serverHost).port(serverPort)
            .addPathSegments(serverBasePath).addPathSegments(PATH).build();
        this.objectMapper = objectMapper;
    }

    private Request buildChatCompletionRequest(IContextManager contextManager, String model, double temperature,
                                               int maxTokens, boolean withStream) throws JsonProcessingException {
        ChatCompletionReq req = new ChatCompletionReq();
        contextManager.iterateChatMessage(message -> {
            if (reduceToken) {
                if (message.getRole() != ChatRole.ASSISTANT) {
                    req.addMessage(message.getRole(), message.getContent());
                }
            } else {
                req.addMessage(message.getRole(), message.getContent());
            }
        });
        req.setModel(model);
        req.setTemperature(temperature);
        req.setMaxTokens(maxTokens);
        req.setStream(withStream);
        RequestBody body = RequestBody.create(objectMapper.writeValueAsBytes(req),
                MediaType.parse("application/json"));
        return new Request.Builder().url(httpUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body).build();
    }

    private void request(IContextManager contextManager, String model, double temperature, int maxTokens)
            throws IOException {
        Response response = httpClient.newCall(buildChatCompletionRequest(contextManager, model, temperature,
                        maxTokens,false)).execute();
        if (response.isSuccessful()) {
            ChatCompletionResp resp = objectMapper.readValue(response.body().string(), ChatCompletionResp.class);
            if (resp != null){
                if(resp.getChoices().length > 0) {
                    ChatMessage message = resp.getChoices()[0].getMessage();
                    if (resp.getUsage() != null) {
                        TokenUsage usage = resp.getUsage();
                        contextManager.completion(message.getRole(), message.getContent(), usage.getPromptTokens(),
                                usage.getCompletionTokens(), usage.getTotalTokens());
                    } else {
                        contextManager.completion(message.getRole(), message.getContent());
                    }
                }
            }
        } else {
            throw new RuntimeException(response.body().string());
        }
    }

    public void ask(String question, IContextManager contextManager) throws IOException {
        contextManager.completion(ChatRole.USER, question);
        request(contextManager, model, temperature, maxTokens);
    }

    private void requestWithSse(IContextManager contextManager, String model, double temperature, int maxTokens,
                                IChatCompletionSseCallback callback) throws JsonProcessingException {
        final SseRespBuffer buffer = new SseRespBuffer();
        RealEventSource eventSource = new RealEventSource(
                buildChatCompletionRequest(contextManager, model, temperature, maxTokens, true),
                new EventSourceListener() {
                    @Override
                    public void onClosed(@NotNull EventSource eventSource) {
                        super.onClosed(eventSource);
                    }

                    @Override
                    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                        if (!data.equals("[DONE]")) {
                            try {
                                ChatCompletionSseResp resp = objectMapper.readValue(data, ChatCompletionSseResp.class);
                                ChatMessage delta = resp.getChoices()[0].getDelta();
                                if (delta != null) {
                                    if (delta.getRole() != null) {
                                        buffer.setRole(delta.getRole());
                                    }
                                    if (delta.getContent() != null) {
                                        buffer.addToken(delta.getContent());
                                        callback.speak(delta.getContent(), buffer.getCompletionTokens(), buffer.getTotalTokens(), null);
                                    }
                                }
                            } catch (JsonProcessingException e) {
                                onFailure(
                                        eventSource, e,
                                        new Response.Builder().code(500).body(ResponseBody
                                                .create(e.getLocalizedMessage(), MediaType.get("text/plain"))).build());
                            }

                        } else {
                            contextManager.completion(buffer.getRole() != null ? buffer.getRole() : ChatRole.ASSISTANT,
                                    buffer.getContent(), 0, buffer.getCompletionTokens(),
                                    buffer.getCompletionTokens());
                            callback.speak(data, buffer.getCompletionTokens(), buffer.getTotalTokens(), null);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                        super.onFailure(eventSource, t, response);
                        if (t != null) {
                            log.error(t.getLocalizedMessage(), t);
                            callback.speak(null, buffer.getCompletionTokens(), buffer.getTotalTokens(), t.getLocalizedMessage());
                            eventSource.cancel();
                        }
                    }

                    @Override
                    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                        super.onOpen(eventSource, response);
                    }
                }
        );
        eventSource.connect(httpClient);
    }

    public void askWithSse(String question, IContextManager contextManager, IChatCompletionSseCallback callback)
            throws JsonProcessingException {
        contextManager.completion(ChatRole.USER, question);
        requestWithSse(contextManager, model, temperature, maxTokens, callback);
    }
}
