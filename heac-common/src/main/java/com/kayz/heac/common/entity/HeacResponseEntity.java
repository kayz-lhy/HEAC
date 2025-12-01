package com.kayz.heac.common.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
public class HeacResponseEntity<T> {
    private String code;
    private String message;
    private T data;

    public static <T> HeacResponseEntity<T> success(T data) {
        return of("200", "success", data);
    }

    public static <T> HeacResponseEntity<T> success(T data, String message) {
        return of("200", message, data);
    }

    public static <T> HeacResponseEntity<T> error(String code, String message) {
        return of(code, message, null);
    }

    public static <T> HeacResponseEntity<T> internalServerError(String message) {
        return error("500", message);
    }

    public static <T> HeacResponseEntity<T> badRequest(String message) {
        return error("400", message);
    }

    public static <T> HeacResponseEntity<T> notFound(String message) {
        return error("404", message);
    }

    public static <T> HeacResponseEntity<T> internalServerError(String message, String code) {
        return error(code, message);
    }

    public static <T> HeacResponseEntity<T> unauthorized(String message) {
        return error("401", message);
    }

    public static <T> HeacResponseEntity<T> notAcceptable(String message) {
        return error("406", message);
    }

    public static <T> HeacResponseEntity<T> methodNotAllowed(String message) {
        return error("405", message);
    }

    public static <T> HeacResponseEntity<T> conflict(String message) {
        return error("409", message);
    }
}