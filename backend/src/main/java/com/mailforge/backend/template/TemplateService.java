package com.mailforge.backend.template;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mailforge.backend.common.ApiException;

@Service
public class TemplateService {

    private final TemplateRepository templates;
    private final CurrentUserProvider currentUser;

    public TemplateService(TemplateRepository templates, CurrentUserProvider currentUser) {
        this.templates = templates;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<TemplateSummary> list(boolean includeArchived) {
        List<Template> found = includeArchived
                ? templates.findAllByOrderByIdAsc()
                : templates.findByArchivedFalseOrderByIdAsc();
        return found.stream().map(TemplateSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public TemplateDetails get(Long id) {
        return TemplateDetails.from(find(id));
    }

    @Transactional
    public TemplateDetails create(TemplateRequest request) {
        Template template = new Template(request.name().strip(), request.htmlBody(),
                currentUser.currentUserId());
        return TemplateDetails.from(templates.save(template));
    }

    @Transactional
    public TemplateDetails update(Long id, TemplateRequest request) {
        Template template = find(id);
        if (template.isArchived()) {
            throw ApiException.conflict("TEMPLATE_ARCHIVED", "Архивный шаблон нельзя изменять");
        }
        template.setName(request.name().strip());
        template.setHtmlBody(request.htmlBody());
        return TemplateDetails.from(templates.saveAndFlush(template));
    }

    @Transactional
    public TemplateDetails archive(Long id) {
        Template template = find(id);
        template.setArchived(true);
        return TemplateDetails.from(templates.saveAndFlush(template));
    }

    private Template find(Long id) {
        return templates.findById(id).orElseThrow(() -> ApiException.notFound("Шаблон"));
    }
}