package org.coodex.openai.api.server.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration("appConfig")
public class ApplicationConfig {

    @Value("${app.token.expire-days}")
    public String tokenExpireDays;
}
