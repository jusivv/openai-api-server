package org.coodex.openai.api.server.domain;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final int LOW_BOUND = 33;
    private static final int HIGH_BOUND = 122;
    private static final int BOUND_SIZE = HIGH_BOUND - LOW_BOUND + 1;
    private static final int DEFAULT_LENGTH = 12;
    private SecureRandom random;

    public PasswordGenerator() {
        random = new SecureRandom();
    }

    public String generate(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) (random.nextInt(BOUND_SIZE) + LOW_BOUND));
        }
        return sb.toString();
    }

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }
}
