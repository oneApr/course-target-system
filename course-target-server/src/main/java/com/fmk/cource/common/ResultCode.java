package com.fmk.cource.common;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "权限不足，无法访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),

    //  业务错误
    BUSINESS_ERROR(500, "业务处理失败"),
    USER_NOT_EXIST(1001, "用户不存在或已停用"),
    PASSWORD_ERROR(1002, "密码错误"),
    DATA_EXIST(1003, "数据已存在"),
    DATA_NOT_EXIST(1004, "数据不存在"),
    FILE_UPLOAD_ERROR(1005, "文件上传失败"),
    EXCEL_PARSE_ERROR(1006, "Excel 解析失败"),

    // 系统错误
    SYSTEM_ERROR(9999, "系统内部错误，请联系管理员");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
