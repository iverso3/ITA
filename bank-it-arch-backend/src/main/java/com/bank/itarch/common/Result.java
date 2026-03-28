package com.bank.itarch.common;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private String timestamp;
    private String traceId;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(LocalDateTime.now().toString());
        result.setTraceId(java.util.UUID.randomUUID().toString());
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now().toString());
        result.setTraceId(java.util.UUID.randomUUID().toString());
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now().toString());
        result.setTraceId(java.util.UUID.randomUUID().toString());
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error() {
        return error(500, "Internal server error");
    }
}
