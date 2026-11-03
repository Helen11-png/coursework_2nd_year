package com.mailforge.backend.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Превращает любые исключения в ответ единого формата ApiError. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ApiError.of(ex.getCode(), ex.getMessage()));
    }

    /** Ошибки проверки полей запроса (@Valid): 400 со списком полей. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldIssue> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.FieldIssue(e.getField(), e.getDefaultMessage()))
                .toList();
        ApiError body = new ApiError("VALIDATION_ERROR", "Проверьте заполнение полей",
                java.time.Instant.now(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of("FORBIDDEN", "Недостаточно прав"));
    }

    /** Всё остальное. Стандартные ошибки Spring (404, 405 и т.д.) сохраняют свой код. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            return ResponseEntity.status(er.getStatusCode())
                    .body(ApiError.of("HTTP_" + er.getStatusCode().value(), er.getBody().getDetail()));
        }
        log.error("Необработанная ошибка", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "Внутренняя ошибка сервера"));
    }
}
