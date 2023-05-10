package org.coodex.openai.api.server.component;

import org.coodex.openai.api.server.model.CommonResp;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebExceptionHandler {
    @ExceptionHandler({ Exception.class })
//    @ResponseBody
    public ResponseEntity<CommonResp> handle(Exception e) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(CommonResp.build(400, e.getLocalizedMessage()));
    }
}
