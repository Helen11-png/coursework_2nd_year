package com.mailforge.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Создаёт администратора по умолчанию при запуске приложения.
 *
 * <p>Логин и пароль берутся из переменных окружения ADMIN_LOGIN и ADMIN_PASSWORD
 * (в Docker их передаёт docker-compose из infra/.env). Логин сохраняется в колонку users.email.
 * <p>Запускается после миграций Flyway, поэтому таблицы и роли уже существуют.
 * Работает через JdbcTemplate, чтобы не зависеть от JPA-сущности пользователя (блок "Вход").
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    static final String DEV_DEFAULT_PASSWORD = "admin12345";

    private static final String INSERT_ADMIN = """
            INSERT INTO users (email, password_hash, full_name, role_id)
            SELECT CAST(? AS VARCHAR), CAST(? AS VARCHAR), CAST(? AS VARCHAR), r.id
            FROM roles r
            WHERE r.code = 'ADMIN'
            ON CONFLICT (email) DO NOTHING
            """;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final Environment env;
    private final String login;
    private final String password;

    public AdminSeeder(JdbcTemplate jdbc,
                       PasswordEncoder encoder,
                       Environment env,
                       @Value("${ADMIN_LOGIN:admin}") String login,
                       @Value("${ADMIN_PASSWORD:}") String password) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.env = env;
        this.login = login;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        String effectivePassword = password;

        if (effectivePassword == null || effectivePassword.isBlank()) {
            if (env.acceptsProfiles(Profiles.of("dev"))) {
                effectivePassword = DEV_DEFAULT_PASSWORD;
                log.warn("ADMIN_PASSWORD не задан, профиль dev: администратор '{}' создаётся с паролем для разработки",
                        login);
            } else {
                log.warn("ADMIN_PASSWORD не задан: администратор по умолчанию не создан");
                return;
            }
        }

        int created = jdbc.update(INSERT_ADMIN, login, encoder.encode(effectivePassword), "Администратор");
        if (created > 0) {
            log.info("Создан администратор по умолчанию: {}", login);
        } else {
            log.info("Администратор '{}' уже существует или роль ADMIN не найдена, ничего не изменено", login);
        }
    }
}
