package org.coodex.openai.api.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coodex.openai.api.server.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan({
		"org.coodex.openai.api.server.data.entity"
})
@EnableJpaRepositories({
		"org.coodex.openai.api.server.data.repo"
})
public class OpenaiApiServerApplication {

	private static Logger log = LoggerFactory.getLogger(OpenaiApiServerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(OpenaiApiServerApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return mapper;
	}

	@Bean
	public HttpSessionIdResolver sessionIdResolver() {
		return new HeaderHttpSessionIdResolver(Const.SESSION_ID_HEADER_NAME);
	}

}
