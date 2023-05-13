package org.coodex.openai.api.server.config;

import org.apache.commons.lang3.ArrayUtils;
import org.coodex.openai.api.server.component.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] RESOURCE_PATTERNS = {
            "/**/*.js",
            "/**/*.css",
            "/**/*.jpg",
            "/**/*.jpeg",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.ico",
            "/**/*.map"
    };

    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/**")
                .excludePathPatterns(ArrayUtils.addAll(RESOURCE_PATTERNS, "/account/login"));
    }
}
