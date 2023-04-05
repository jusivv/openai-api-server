package org.coodex.openai.api.server.util;

public class Counter {
    private int count;

    public Counter() {
        this.count = 0;
    }

    public int next() {
        return ++count;
    }

    public String nextString() {
        return String.valueOf(next());
    }
}
