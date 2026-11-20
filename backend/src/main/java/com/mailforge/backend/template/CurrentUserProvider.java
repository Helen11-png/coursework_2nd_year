package com.mailforge.backend.template;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Возвращает id текущего пользователя. Входа пока нет, поэтому это администратор по умолчанию
 * (его логин лежит в users.email). Когда появится вход, заменить реализацию на пользователя из сессии.
 */
@Component
public class CurrentUserProvider {

    private final JdbcTemplate jdbc;
    private final String adminLogin;

    public CurrentUserProvider(JdbcTemplate jdbc, @Value("${ADMIN_LOGIN:admin}") String adminLogin) {
        this.jdbc = jdbc;
        this.adminLogin = adminLogin;
    }

    public Long currentUserId() {
        List<Long> ids = jdbc.queryForList("SELECT id FROM users WHERE email = ?", Long.class, adminLogin);
        if (ids.isEmpty()) {
            // Ошибка настройки, а не пользователя: клиент получит общий INTERNAL_ERROR, детали попадут в лог
            throw new IllegalStateException(
                    "Администратор по умолчанию '" + adminLogin + "' не найден в таблице users");
        }
        return ids.get(0);
    }
}