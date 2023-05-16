package org.coodex.openai.api.server.component;

import org.coodex.openai.api.server.model.CommonResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);
    @ExceptionHandler({ Exception.class })
    public ResponseEntity<CommonResp> handle(Exception e) {
        log.error(e.getLocalizedMessage(), e);
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(CommonResp.build(400, e.getLocalizedMessage()));
    }
}
