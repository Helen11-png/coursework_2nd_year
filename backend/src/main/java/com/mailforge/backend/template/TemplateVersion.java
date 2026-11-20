package com.mailforge.backend.template;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "template_versions")
public class TemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "html_body", nullable = false, columnDefinition = "text")
    private String htmlBody;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    protected TemplateVersion() {}

    public TemplateVersion(Long templateId, int versionNo, String htmlBody, Long createdBy) {
        this.templateId = templateId;
        this.versionNo = versionNo;
        this.htmlBody = htmlBody;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public int getVersionNo() { return versionNo; }
    public String getHtmlBody() { return htmlBody; }
    public Long getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}