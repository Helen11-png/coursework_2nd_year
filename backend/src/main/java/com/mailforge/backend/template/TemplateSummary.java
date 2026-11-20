package com.mailforge.backend.template;

import java.time.Instant;

/** Краткая карточка шаблона для списка (без тела письма). */
public record TemplateSummary(
        Long id,
        String name,
        Long ownerId,
        boolean archived,
        Instant createdAt,
        Instant updatedAt) {

    public static TemplateSummary from(Template t) {
        return new TemplateSummary(t.getId(), t.getName(), t.getOwnerId(),
                t.isArchived(), t.getCreatedAt(), t.getUpdatedAt());
    }
}