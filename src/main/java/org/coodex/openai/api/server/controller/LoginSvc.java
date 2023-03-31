package org.coodex.openai.api.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSvc {
    @GetMapping("/")
    public String index() {
        return "chat";
    }
}
