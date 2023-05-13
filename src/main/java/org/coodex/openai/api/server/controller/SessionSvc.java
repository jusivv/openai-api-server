package org.coodex.openai.api.server.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotBlank;
import org.coodex.openai.api.server.domain.HttpSessionHolder;
import org.coodex.openai.api.server.model.CommonResp;
import org.coodex.openai.api.server.model.LoginAccount;
import org.coodex.openai.api.server.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("session")
public class SessionSvc {
    private HttpSessionHolder sessionHolder;

    @Autowired
    public SessionSvc(HttpSessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    @GetMapping("list")
    @RolesAllowed({ Const.ROLE_ADMIN })
    public LoginAccount[] listSession() {
        return sessionHolder.listAccounts();
    }

    @GetMapping("kickOut/{accountId}")
    @RolesAllowed({Const.ROLE_ADMIN})
    public CommonResp kickOut(@PathVariable("accountId") @NotBlank String accountId) {
        sessionHolder.kickOut(accountId);
        return CommonResp.build(200, accountId);
    }
}
