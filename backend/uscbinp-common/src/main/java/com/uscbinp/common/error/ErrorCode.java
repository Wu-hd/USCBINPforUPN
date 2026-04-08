package com.uscbinp.common.error;

public enum ErrorCode {
    SUCCESS("00000", "OK"),
    VALIDATION_ERROR("REQ_4000", "请求参数不合法"),
    BUSINESS_ERROR("BIZ_4001", "业务处理失败"),
    AUTH_FORBIDDEN("AUTH_4030", "无权限访问"),
    SYSTEM_ERROR("SYS_5000", "系统内部错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
