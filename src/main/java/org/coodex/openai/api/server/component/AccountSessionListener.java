package org.coodex.openai.api.server.component;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.coodex.openai.api.server.domain.HttpSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountSessionListener implements HttpSessionListener {

    private HttpSessionHolder sessionHolder;

    @Autowired
    public AccountSessionListener(HttpSessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        sessionHolder.add(se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        sessionHolder.remove(se.getSession());
    }
}
