package org.coodex.openai.api.server.model;

public class CommonResp {
    private int code;
    private String message;

    public static CommonResp build(int code, String message) {
        CommonResp resp = new CommonResp();
        resp.code = code;
        resp.message = message;
        return resp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
