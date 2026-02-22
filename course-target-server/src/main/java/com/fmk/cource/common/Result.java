package com.fmk.cource.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 使用 ResultCode 枚举统一管理响应码
 */
@Data
public class Result<T> implements Serializable {

    /** 响应码 */
    private int code;
    /** 响应消息 */
    private String msg;
    /** 响应数据 */
    private T data;

    private Result() {}

    // ==================== 成功 ====================

    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> r = build(ResultCode.SUCCESS, data);
        r.setMsg(msg);
        return r;
    }

    // ==================== 失败 ====================

    public static <T> Result<T> error() {
        return build(ResultCode.SYSTEM_ERROR, null);
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = build(ResultCode.SYSTEM_ERROR, null);
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /** 使用 ResultCode 枚举返回错误 */
    public static <T> Result<T> error(ResultCode resultCode) {
        return build(resultCode, null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String customMsg) {
        Result<T> r = build(resultCode, null);
        r.setMsg(customMsg);
        return r;
    }


    private static <T> Result<T> build(ResultCode resultCode, T data) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        r.setData(data);
        return r;
    }
}
