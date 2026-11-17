package com.mailforge.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailforge.backend.config.AdminSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "ADMIN_LOGIN=seed-admin@test.local",
        "ADMIN_PASSWORD=Secret-123"
})
class AdminSeederTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AdminSeeder seeder;

    @Test
    void createsAdminOnlyOnce() {
        seeder.run(null);

        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM users WHERE email = ?", Integer.class, "seed-admin@test.local");
        assertThat(count).isEqualTo(1);

        String roleCode = jdbc.queryForObject(
                "SELECT r.code FROM users u JOIN roles r ON r.id = u.role_id WHERE u.email = ?",
                String.class, "seed-admin@test.local");
        assertThat(roleCode).isEqualTo("ADMIN");

        String hash = jdbc.queryForObject(
                "SELECT password_hash FROM users WHERE email = ?", String.class, "seed-admin@test.local");
        assertThat(hash).isNotEqualTo("Secret-123");
        assertThat(encoder.matches("Secret-123", hash)).isTrue();
    }
}
