package com.mailforge.backend.common;

import java.time.Instant;
import java.util.List;

/** Единый формат ошибки для всех запросов API: код, сообщение, поля с ошибками. */
public record ApiError(String code, String message, Instant timestamp, List<FieldIssue> fields) {

    public record FieldIssue(String field, String message) {}

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Instant.now(), List.of());
    }
}
