package com.mailforge.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.mailforge.backend.template.Template;
import com.mailforge.backend.template.TemplateRepository;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "ADMIN_LOGIN=tpl-admin@test.local",
        "ADMIN_PASSWORD=Secret-123"
})
class TemplateApiTest {

    private static final String ADMIN_LOGIN = "tpl-admin@test.local";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TemplateRepository repository;

    private Long adminId;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM templates");
        adminId = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, ADMIN_LOGIN);
    }

    private Template save(String name, String html) {
        return repository.saveAndFlush(new Template(name, html, adminId));
    }

    // ---------- POST /api/templates ----------

    @Test
    void createSavesTemplateOwnedByDefaultAdmin() throws Exception {
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Welcome", "htmlBody": "<h1>Hello</h1>"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Welcome"))
                .andExpect(jsonPath("$.htmlBody").value("<h1>Hello</h1>"))
                .andExpect(jsonPath("$.archived").value(false));

        Long ownerId = jdbc.queryForObject(
                "SELECT owner_id FROM templates WHERE name = ?", Long.class, "Welcome");
        assertThat(ownerId).isEqualTo(adminId);
    }

    @Test
    void createRejectsBlankFields() throws Exception {
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "htmlBody": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.length()").value(2));

        Integer count = jdbc.queryForObject("SELECT count(*) FROM templates", Integer.class);
        assertThat(count).isZero();
    }

    // ---------- GET /api/templates ----------

    @Test
    void listReturnsTemplatesWithoutHtmlBody() throws Exception {
        save("Welcome", "<h1>Hello</h1>");

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Welcome"))
                .andExpect(jsonPath("$[0].htmlBody").doesNotExist());
    }

    // ---------- GET /api/templates/{id} ----------

    @Test
    void getReturnsTemplateWithHtmlBody() throws Exception {
        Template saved = save("Welcome", "<h1>Hello</h1>");

        mockMvc.perform(get("/api/templates/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Welcome"))
                .andExpect(jsonPath("$.htmlBody").value("<h1>Hello</h1>"));
    }

    @Test
    void getMissingTemplateReturns404() throws Exception {
        mockMvc.perform(get("/api/templates/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ---------- PUT /api/templates/{id} ----------

    @Test
    void updateChangesNameAndBody() throws Exception {
        Template saved = save("Old name", "<p>old</p>");

        mockMvc.perform(put("/api/templates/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "New name", "htmlBody": "<p>new</p>"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.htmlBody").value("<p>new</p>"));

        String name = jdbc.queryForObject(
                "SELECT name FROM templates WHERE id = ?", String.class, saved.getId());
        assertThat(name).isEqualTo("New name");
    }

    @Test
    void updateArchivedTemplateReturns409() throws Exception {
        Template saved = save("Old name", "<p>old</p>");
        saved.setArchived(true);
        repository.saveAndFlush(saved);

        mockMvc.perform(put("/api/templates/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "New name", "htmlBody": "<p>new</p>"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TEMPLATE_ARCHIVED"));
    }

    // ---------- POST /api/templates/{id}/archive ----------

    @Test
    void archiveHidesTemplateFromDefaultList() throws Exception {
        Template saved = save("Welcome", "<h1>Hello</h1>");

        mockMvc.perform(post("/api/templates/{id}/archive", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/templates").param("includeArchived", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}