package org.coodex.openai.api.server.util;

import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Component;

@Component
public class IDGenerator {

    private static ULID ulid = new ULID();

    public static String genId() {
        return ulid.nextULID();
    }
}
