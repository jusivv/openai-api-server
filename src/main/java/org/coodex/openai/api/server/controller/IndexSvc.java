package org.coodex.openai.api.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexSvc {
    private static Logger log = LoggerFactory.getLogger(IndexSvc.class);
    @GetMapping("/")
    public String index() {
        return "main";
    }

    @GetMapping("/{sfc}.vue")
    public String loadSfc(@PathVariable("sfc") String sfc) {
        return sfc;
    }
}

