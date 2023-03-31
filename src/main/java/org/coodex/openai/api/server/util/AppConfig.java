package org.coodex.openai.api.server.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration("app")
public class AppConfig {
    @Value("${app.serverUrl}")
    public String serverUrl;
    @Value("${openai.maxTokens}")
    public int maxTokens;
}
