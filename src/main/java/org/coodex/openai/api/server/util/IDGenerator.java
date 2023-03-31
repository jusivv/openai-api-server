package org.coodex.openai.api.server.util;

import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Component;

@Component
public class IDGenerator {

    private ULID ulid;

    public IDGenerator() {
        ulid = new ULID();
    }

    public String genId() {
        return ulid.nextULID();
    }
}
