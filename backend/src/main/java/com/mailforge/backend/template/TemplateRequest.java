package com.mailforge.backend.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Тело запроса на создание и изменение шаблона. */
public record TemplateRequest(
        @NotBlank(message = "не должно быть пустым")
        @Size(max = 200, message = "не длиннее 200 символов")
        String name,

        @NotBlank(message = "не должно быть пустым")
        String htmlBody) {
}