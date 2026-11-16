-- =====================================================================
-- V1: начальная схема MailForge по ERD (версия после ревью Сони и Артёма).
-- Правила:
--   * первичный ключ везде называется id;
--   * внешние ключи называются <таблица_в_ед_числе>_id (template_id, client_id...);
--   * время хранится как TIMESTAMPTZ;
--   * статусы и коды пишем ЗАГЛАВНЫМИ буквами, в Java значения должны совпадать буква в букву.
-- Пока проект не задеплоен, файл можно переписывать. После первого деплоя
-- изменения делаем только новыми файлами V2, V3...
-- =====================================================================


CREATE TABLE roles (
    id   SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(16)  NOT NULL UNIQUE CHECK (code IN ('MARKETER', 'ANALYST', 'ADMIN')),
    name VARCHAR(64)  NOT NULL
);

INSERT INTO roles (code, name) VALUES
    ('MARKETER', 'Маркетолог'),
    ('ANALYST',  'Аналитик'),
    ('ADMIN',    'Администратор');

CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(254) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    role_id         SMALLINT     NOT NULL REFERENCES roles (id),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INT          NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);


CREATE TABLE templates (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    owner_id   BIGINT       NOT NULL REFERENCES users (id),
    html_body  TEXT         NOT NULL,
    archived   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_templates_owner ON templates (owner_id);

CREATE TABLE template_versions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_id BIGINT      NOT NULL REFERENCES templates (id),
    version_no  INT         NOT NULL,
    html_body   TEXT        NOT NULL,
    created_by  BIGINT      NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_template_versions_no UNIQUE (template_id, version_no),
    CONSTRAINT uq_template_versions_tpl_id UNIQUE (template_id, id)
);

CREATE TABLE import_batches (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_name     VARCHAR(255) NOT NULL,
    format        VARCHAR(8)   NOT NULL CHECK (format IN ('CSV', 'JSON')),
    total_rows    INT          NOT NULL,
    accepted_rows INT          NOT NULL,
    rejected_rows INT          NOT NULL,
    imported_by   BIGINT       NOT NULL REFERENCES users (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE clients (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(254) NOT NULL,
    full_name       VARCHAR(200),
    consent_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
    consent_at      TIMESTAMPTZ,
    is_email_valid  BOOLEAN      NOT NULL DEFAULT TRUE,
    import_batch_id BIGINT       REFERENCES import_batches (id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_clients_email ON clients (lower(email));
CREATE INDEX ix_clients_import_batch ON clients (import_batch_id);

CREATE TABLE segments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by  BIGINT       NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE segment_clients (
    segment_id BIGINT NOT NULL REFERENCES segments (id) ON DELETE CASCADE,
    client_id  BIGINT NOT NULL REFERENCES clients (id)  ON DELETE CASCADE,
    PRIMARY KEY (segment_id, client_id)
);
CREATE INDEX ix_segment_clients_client ON segment_clients (client_id);

CREATE TABLE campaigns (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    template_id         BIGINT       NOT NULL REFERENCES templates (id),
    template_version_id BIGINT,
    segment_id          BIGINT       NOT NULL REFERENCES segments (id),
    scheduled_at        TIMESTAMPTZ,
    status              VARCHAR(16)  NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'SCHEDULED', 'PROCESSING', 'FINISHED', 'FAILED')),
    created_by          BIGINT       NOT NULL REFERENCES users (id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_campaigns_version_required
        CHECK (status = 'DRAFT' OR template_version_id IS NOT NULL),
    CONSTRAINT fk_campaigns_template_version
        FOREIGN KEY (template_id, template_version_id)
        REFERENCES template_versions (template_id, id)
);
CREATE INDEX ix_campaigns_status   ON campaigns (status);
CREATE INDEX ix_campaigns_template ON campaigns (template_id);
CREATE INDEX ix_campaigns_segment  ON campaigns (segment_id);


CREATE TABLE delivery_log (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    campaign_id   BIGINT       NOT NULL REFERENCES campaigns (id),
    client_id     BIGINT       NOT NULL REFERENCES clients (id),
    request_id    VARCHAR(64)  NOT NULL,
    status        VARCHAR(8)   NOT NULL CHECK (status IN ('QUEUED', 'SENT', 'FAILED')),
    attempt_count INT          NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    queued_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ,
    last_error    TEXT,
    CONSTRAINT uq_delivery_request_client UNIQUE (request_id, client_id)
);
CREATE INDEX ix_delivery_campaign_client ON delivery_log (campaign_id, client_id);
CREATE INDEX ix_delivery_status          ON delivery_log (status);
CREATE INDEX ix_delivery_next_retry      ON delivery_log (next_retry_at) WHERE next_retry_at IS NOT NULL;

CREATE TABLE delivery_attempts (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    delivery_log_id   BIGINT      NOT NULL REFERENCES delivery_log (id),
    attempt_no        INT         NOT NULL,
    status            VARCHAR(8)  NOT NULL CHECK (status IN ('SENT', 'FAILED')),
    provider_response TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_attempt_no UNIQUE (delivery_log_id, attempt_no)
);

CREATE TABLE audit_log (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      REFERENCES users (id),   
    action      VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id   BIGINT,
    details     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_created_at ON audit_log (created_at DESC);

CREATE TABLE settings (
    key   VARCHAR(64)  PRIMARY KEY,
    value VARCHAR(256) NOT NULL
);

INSERT INTO settings (key, value) VALUES
    ('mock.failure_probability', '10'),
    ('retry.max_attempts', '3'),
    ('retry.delay_seconds', '30'),
    ('kpi.success_threshold', '99');