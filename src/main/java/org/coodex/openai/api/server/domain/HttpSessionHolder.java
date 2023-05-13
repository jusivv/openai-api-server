package org.coodex.openai.api.server.domain;

import jakarta.servlet.http.HttpSession;
import org.coodex.openai.api.server.model.LoginAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Component
public class HttpSessionHolder {
    private static Logger log = LoggerFactory.getLogger(HttpSessionHolder.class);
    private Map<String, HttpSession> sessionMap;

    public HttpSessionHolder() {
        sessionMap = new Hashtable<>();
    }

    public HttpSession get(String accountId) {
        if (accountId != null) {
            for (HttpSession session : sessionMap.values()) {
                LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
                if (loginAccount != null && accountId.equals(loginAccount.getAccountId())) {
                    return session;
                }
            }
        }
        return null;
    }

    public boolean kickOut(String accountId) {
        HttpSession session = get(accountId);
        if (session != null) {
            session.invalidate();
            log.debug("kick out accountId [{}]", accountId);
            return true;
        }
        return false;
    }

    public void add(HttpSession session) {
        if (session != null) {
            sessionMap.put(session.getId(), session);
        }
    }

    public void remove(HttpSession session) {
        if (session != null) {
            remove(session.getId());
        }
    }

    public void remove(String sessionId) {
        if (sessionId != null) {
            sessionMap.remove(sessionId);
        }
    }

    public LoginAccount[] listAccounts() {
        List<LoginAccount> list = new ArrayList<>();
        sessionMap.values().forEach(session -> {
            LoginAccount loginAccount = (LoginAccount) session.getAttribute(LoginAccount.SESSION_NAME);
            if (loginAccount != null ) {
                list.add(loginAccount);
            }
        });
        return list.toArray(new LoginAccount[0]);
    }

    public int totalSession() {
        return sessionMap.size();
    }
}
