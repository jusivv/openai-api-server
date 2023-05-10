package org.coodex.openai.api.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexSvc {
    @GetMapping("/")
    public String index(HttpSession session) {
        return "main";
    }
}

