package com.mailforge.backend.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findAllByOrderByIdAsc();

    List<Template> findByArchivedFalseOrderByIdAsc();
}