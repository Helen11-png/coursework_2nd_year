package com.mailforge.backend.template;

import java.time.Instant;

/** Полный шаблон, включая HTML. */
public record TemplateDetails(
        Long id,
        String name,
        Long ownerId,
        String htmlBody,
        boolean archived,
        Instant createdAt,
        Instant updatedAt) {

    public static TemplateDetails from(Template t) {
        return new TemplateDetails(t.getId(), t.getName(), t.getOwnerId(), t.getHtmlBody(),
                t.isArchived(), t.getCreatedAt(), t.getUpdatedAt());
    }
}