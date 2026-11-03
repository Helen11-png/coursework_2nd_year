-- =====================================================================
-- V1: начальная схема. ЧЕРНОВИК по кейсу и нашим решениям.
-- Пока проект не задеплоен, этот файл можно переписывать после утверждения ERD.
-- После первого деплоя изменения делаем только новыми файлами V2, V3...
-- =====================================================================

CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login           VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    role            VARCHAR(16)  NOT NULL CHECK (role IN ('MARKETER', 'ANALYST', 'ADMIN')),
    blocked         BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_attempts INT          NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE clients (
    client_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email        VARCHAR(254) NOT NULL,
    segment      VARCHAR(64)  NOT NULL,
    consent_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_clients_email ON clients (lower(email));
CREATE INDEX ix_clients_segment ON clients (segment);

-- Шаблоны писем (текущая версия) и их история
CREATE TABLE templates (
    tpl_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    html_body  TEXT         NOT NULL,
    version    INT          NOT NULL DEFAULT 1,
    archived   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by BIGINT       REFERENCES users (id)
);

CREATE TABLE template_versions (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tpl_id     BIGINT      NOT NULL REFERENCES templates (tpl_id) ON DELETE CASCADE,
    version_no INT         NOT NULL,
    html_body  TEXT        NOT NULL,
    created_by BIGINT      REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tpl_id, version_no)
);

-- Кампании. Статусы: draft -> scheduled -> processing -> finished / failed.
CREATE TABLE campaigns (
    camp_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    tpl_id              BIGINT       NOT NULL REFERENCES templates (tpl_id),
    template_version_id BIGINT       REFERENCES template_versions (id),
    segment             VARCHAR(64)  NOT NULL,
    scheduled_at        TIMESTAMPTZ,
    status              VARCHAR(16)  NOT NULL DEFAULT 'draft'
                        CHECK (status IN ('draft', 'scheduled', 'processing', 'finished', 'failed')),
    created_by          BIGINT       REFERENCES users (id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    launched_at         TIMESTAMPTZ
);
CREATE INDEX ix_campaigns_status ON campaigns (status);

-- Журнал доставки. Для пары (кампания, клиент) допускается несколько записей (ретраи),
-- итог считается по последнему ts. request_id делает повторный вызов идемпотентным.
CREATE TABLE delivery_log (
    log_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    camp_id    BIGINT      NOT NULL REFERENCES campaigns (camp_id),
    client_id  BIGINT      NOT NULL REFERENCES clients (client_id),
    status     VARCHAR(8)  NOT NULL CHECK (status IN ('queued', 'sent', 'failed')),
    ts         TIMESTAMPTZ NOT NULL DEFAULT now(),
    error      VARCHAR(64),
    request_id VARCHAR(64) UNIQUE,
    attempt_no INT         NOT NULL DEFAULT 1
);
CREATE INDEX ix_delivery_camp_client_ts ON delivery_log (camp_id, client_id, ts DESC);
CREATE INDEX ix_delivery_status ON delivery_log (status);

-- Сессии импорта клиентов (шаг "проверка" перед записью)
CREATE TABLE import_sessions (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    status     VARCHAR(16) NOT NULL DEFAULT 'checked',
    valid_rows INT         NOT NULL DEFAULT 0,
    error_rows INT         NOT NULL DEFAULT 0,
    payload    JSONB,
    errors     JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);

-- Настройки (вероятность ошибки мока, ретраи, порог KPI)
CREATE TABLE settings (
    key   VARCHAR(64) PRIMARY KEY,
    value VARCHAR(256) NOT NULL
);
INSERT INTO settings (key, value) VALUES
    ('mock.failure_probability', '10'),   -- % ошибок мок-отправки
    ('retry.max_attempts', '3'),
    ('retry.delay_seconds', '30'),
    ('kpi.success_threshold', '99');

-- Журнал действий (аудит)
CREATE TABLE audit_log (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      REFERENCES users (id),
    action      VARCHAR(64) NOT NULL,
    object_type VARCHAR(32),
    object_id   VARCHAR(64),
    details     VARCHAR(500),
    ts          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_ts ON audit_log (ts DESC);
