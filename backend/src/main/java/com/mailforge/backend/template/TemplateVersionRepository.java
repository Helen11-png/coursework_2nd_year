package com.mailforge.backend.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

    List<TemplateVersion> findByTemplateIdOrderByVersionNoDesc(Long templateId);
}