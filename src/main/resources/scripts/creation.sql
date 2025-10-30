-- =====================================================
-- ИСПРАВЛЕННАЯ СХЕМА БАЗЫ ДАННЫХ
-- Все связи (Foreign Keys) явно указаны и проверены
-- =====================================================

SET search_path TO public;

-- =====================================================
-- 1. СПРАВОЧНЫЕ ТАБЛИЦЫ (Справочники)
-- =====================================================

-- Таблица: roles (Роли сотрудников банка)
CREATE TABLE roles
(
    id          SERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT check_role_name_not_empty CHECK (role_name <> '')
);

COMMENT ON TABLE roles IS 'Справочник ролей сотрудников банка';

-- Таблица: request_statuses (Статусы заявок)
-- ⚠️ ВАЖНО: Эта таблица должна быть создана ДО card_requests
CREATE TABLE request_statuses
(
    id          SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT check_req_status_name_not_empty CHECK (status_name <> '')
);

COMMENT ON TABLE request_statuses IS 'Справочник статусов заявок на карты (используется в card_requests)';

-- Таблица: card_statuses (Статусы карт)
-- ⚠️ ВАЖНО: Эта таблица должна быть создана ДО cards
CREATE TABLE card_statuses
(
    id          SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT check_card_status_name_not_empty CHECK (status_name <> '')
);

COMMENT ON TABLE card_statuses IS 'Справочник статусов выпущенных карт (используется в cards)';

-- Таблица: card_types (Типы карт)
CREATE TABLE card_types
(
    id          SERIAL PRIMARY KEY,
    type_name   VARCHAR(50)    NOT NULL UNIQUE,
    description TEXT,
    annual_fee  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency    VARCHAR(3)     NOT NULL DEFAULT 'RUB',
    CONSTRAINT check_type_name_not_empty CHECK (type_name <> ''),
    CONSTRAINT check_annual_fee_positive CHECK (annual_fee >= 0),
    CONSTRAINT check_currency_length CHECK (LENGTH(currency) = 3)
);

COMMENT ON TABLE card_types IS 'Справочник типов корпоративных карт';

-- Таблица: branches (Отделения банка)
CREATE TABLE branches
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    TEXT         NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    work_hours VARCHAR(100) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT check_branch_name_not_empty CHECK (name <> ''),
    CONSTRAINT check_branch_address_not_empty CHECK (address <> ''),
    CONSTRAINT check_branch_phone_not_empty CHECK (phone <> '')
);

COMMENT ON TABLE branches IS 'Отделения банка для выдачи карт';

-- =====================================================
-- 2. ТАБЛИЦЫ СОТРУДНИКОВ И ОРГАНИЗАЦИЙ
-- =====================================================

-- Таблица: employees (Сотрудники банка)
CREATE TABLE employees
(
    id            SERIAL PRIMARY KEY,
    role_id       INTEGER      NOT NULL,
    branch_id     INTEGER,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    middle_name   VARCHAR(100),
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE SET NULL,
    CONSTRAINT check_employee_first_name CHECK (first_name <> ''),
    CONSTRAINT check_employee_last_name CHECK (last_name <> ''),
    CONSTRAINT check_employee_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT check_employee_phone CHECK (phone <> '')
);

COMMENT ON TABLE employees IS 'Сотрудники банка, работающие в системе';

CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_employees_role_id ON employees (role_id);
CREATE INDEX idx_employees_branch_id ON employees (branch_id);

-- Таблица: organizations (Организации-клиенты)
CREATE TABLE organizations
(
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    inn            VARCHAR(12)  NOT NULL UNIQUE,
    kpp            VARCHAR(9),
    legal_address  TEXT         NOT NULL,
    actual_address TEXT,
    phone          VARCHAR(20)  NOT NULL,
    email          VARCHAR(100) NOT NULL,
    director_name  VARCHAR(255) NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_org_name_not_empty CHECK (name <> ''),
    CONSTRAINT check_inn_length CHECK (LENGTH(inn) IN (10, 12)),
    CONSTRAINT check_kpp_length CHECK (kpp IS NULL OR LENGTH(kpp) = 9),
    CONSTRAINT check_org_legal_address CHECK (legal_address <> ''),
    CONSTRAINT check_org_phone CHECK (phone <> ''),
    CONSTRAINT check_org_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
    );

COMMENT ON TABLE organizations IS 'Организации-клиенты банка';

CREATE INDEX idx_organizations_inn ON organizations (inn);

-- Таблица: users (Физические лица - потенциальные владельцы карт)
CREATE TABLE users
(
    id                  SERIAL PRIMARY KEY,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    middle_name         VARCHAR(100),
    passport_series     VARCHAR(10)  NOT NULL,
    passport_number     VARCHAR(10)  NOT NULL,
    passport_issued_by  TEXT         NOT NULL,
    passport_issue_date DATE         NOT NULL,
    birth_date          DATE         NOT NULL,
    phone               VARCHAR(20)  NOT NULL,
    email               VARCHAR(100),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_user_first_name CHECK (first_name <> ''),
    CONSTRAINT check_user_last_name CHECK (last_name <> ''),
    CONSTRAINT check_passport_series CHECK (passport_series <> ''),
    CONSTRAINT check_passport_number CHECK (passport_number <> ''),
    CONSTRAINT check_birth_date_valid CHECK (birth_date < CURRENT_DATE),
    CONSTRAINT check_user_age CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'),
    CONSTRAINT check_user_email_format CHECK (email IS NULL OR
                                              email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT unique_passport UNIQUE (passport_series, passport_number)
);

COMMENT ON TABLE users IS 'Физические лица - потенциальные владельцы корпоративных карт';

CREATE INDEX idx_users_passport ON users (passport_series, passport_number);
CREATE INDEX idx_users_email ON users (email);

-- Таблица: user_organizations (Связь пользователей с организациями)
CREATE TABLE user_organizations
(
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER      NOT NULL,
    organization_id INTEGER      NOT NULL,
    position        VARCHAR(100) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    date_from       DATE         NOT NULL DEFAULT CURRENT_DATE,
    date_to         DATE,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_org_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_org_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT check_position_not_empty CHECK (position <> ''),
    CONSTRAINT check_date_range_valid CHECK (date_to IS NULL OR date_to >= date_from)
);

COMMENT ON TABLE user_organizations IS 'Связь физических лиц с организациями';

CREATE INDEX idx_user_org_user_id ON user_organizations (user_id);
CREATE INDEX idx_user_org_organization_id ON user_organizations (organization_id);
CREATE INDEX idx_user_org_is_active ON user_organizations (user_id, organization_id, is_active);

CREATE UNIQUE INDEX idx_user_org_active_unique
    ON user_organizations (user_id, organization_id)
    WHERE is_active = TRUE;

-- =====================================================
-- 3. ОСНОВНЫЕ ОПЕРАЦИОННЫЕ ТАБЛИЦЫ
-- =====================================================

-- Таблица: card_requests (Заявки на карты)
-- 🔗 СВЯЗИ:
--   1. user_organization_id → user_organizations(id)
--   2. card_type_id → card_types(id)
--   3. branch_id → branches(id)
--   4. status_id → request_statuses(id) ⚠️ ВАЖНАЯ СВЯЗЬ
--   5. assigned_employee_id → employees(id)
CREATE TABLE card_requests
(
    id                   SERIAL PRIMARY KEY,
    user_organization_id INTEGER     NOT NULL,
    card_type_id         INTEGER     NOT NULL,
    branch_id            INTEGER     NOT NULL,
    status_id            INTEGER     NOT NULL,  -- ⚠️ Связь с request_statuses
    assigned_employee_id INTEGER,
    request_number       VARCHAR(50) NOT NULL UNIQUE,
    comments             TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_user_org FOREIGN KEY (user_organization_id)
        REFERENCES user_organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_card_type FOREIGN KEY (card_type_id)
        REFERENCES card_types (id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE RESTRICT,
    -- ⚠️ ВАЖНАЯ СВЯЗЬ #1: card_requests → request_statuses
    CONSTRAINT fk_request_status FOREIGN KEY (status_id)
        REFERENCES request_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_employee FOREIGN KEY (assigned_employee_id)
        REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT check_request_number CHECK (request_number <> '')
);

COMMENT ON TABLE card_requests IS 'Заявки на выпуск корпоративных карт';
COMMENT ON COLUMN card_requests.status_id IS '⚠️ Внешний ключ на request_statuses - СВЯЗЬ УСТАНОВЛЕНА';

CREATE INDEX idx_requests_user_org_id ON card_requests (user_organization_id);
CREATE INDEX idx_requests_status_id ON card_requests (status_id);
CREATE INDEX idx_requests_assigned_employee ON card_requests (assigned_employee_id);
CREATE INDEX idx_requests_created_at ON card_requests (created_at);
CREATE INDEX idx_requests_card_type_id ON card_requests (card_type_id);

-- Таблица: cards (Выпущенные карты)
-- 🔗 СВЯЗИ:
--   1. request_id → card_requests(id)
--   2. card_type_id → card_types(id)
--   3. status_id → card_statuses(id) ⚠️ ВАЖНАЯ СВЯЗЬ
CREATE TABLE cards
(
    id              SERIAL PRIMARY KEY,
    request_id      INTEGER      NOT NULL UNIQUE,
    card_type_id    INTEGER      NOT NULL,
    status_id       INTEGER      NOT NULL,  -- ⚠️ Связь с card_statuses
    card_number     VARCHAR(16)  NOT NULL UNIQUE,
    cardholder_name VARCHAR(100) NOT NULL,
    expiry_date     DATE         NOT NULL,
    issue_date      DATE         NOT NULL DEFAULT CURRENT_DATE,
    cvv_hash        VARCHAR(255) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_request FOREIGN KEY (request_id)
        REFERENCES card_requests (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_type FOREIGN KEY (card_type_id)
        REFERENCES card_types (id) ON DELETE RESTRICT,
    -- ⚠️ ВАЖНАЯ СВЯЗЬ #2: cards → card_statuses
    CONSTRAINT fk_card_status FOREIGN KEY (status_id)
        REFERENCES card_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT check_card_number_length CHECK (LENGTH(card_number) = 16),
    CONSTRAINT check_card_number_digits CHECK (card_number ~ '^[0-9]{16}$'),
    CONSTRAINT check_cardholder_name CHECK (cardholder_name <> ''),
    CONSTRAINT check_expiry_date_future CHECK (expiry_date > issue_date)
);

COMMENT ON TABLE cards IS 'Выпущенные корпоративные карты';
COMMENT ON COLUMN cards.status_id IS '⚠️ Внешний ключ на card_statuses - СВЯЗЬ УСТАНОВЛЕНА';

CREATE INDEX idx_cards_request_id ON cards (request_id);
CREATE INDEX idx_cards_card_number ON cards (card_number);
CREATE INDEX idx_cards_status_id ON cards (status_id);

-- Таблица: card_limits (Лимиты карт)
CREATE TABLE card_limits
(
    id                       SERIAL PRIMARY KEY,
    card_id                  INTEGER        NOT NULL,
    daily_limit              DECIMAL(15, 2) NOT NULL,
    monthly_limit            DECIMAL(15, 2) NOT NULL,
    single_transaction_limit DECIMAL(15, 2) NOT NULL,
    atm_daily_limit          DECIMAL(15, 2) NOT NULL,
    currency                 VARCHAR(3)     NOT NULL DEFAULT 'RUB',
    updated_at               TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_limit_card FOREIGN KEY (card_id)
        REFERENCES cards (id) ON DELETE CASCADE,
    CONSTRAINT check_daily_limit_positive CHECK (daily_limit > 0),
    CONSTRAINT check_monthly_limit_positive CHECK (monthly_limit > 0),
    CONSTRAINT check_single_limit_positive CHECK (single_transaction_limit > 0),
    CONSTRAINT check_atm_limit_positive CHECK (atm_daily_limit >= 0),
    CONSTRAINT check_daily_less_monthly CHECK (daily_limit <= monthly_limit),
    CONSTRAINT check_single_less_daily CHECK (single_transaction_limit <= daily_limit),
    CONSTRAINT check_limit_currency_length CHECK (LENGTH(currency) = 3)
);

COMMENT ON TABLE card_limits IS 'Лимиты операций по картам';

CREATE INDEX idx_card_limits_card_id ON card_limits (card_id);

-- Таблица: card_type_limit_templates (Шаблоны лимитов для типов карт)
CREATE TABLE card_type_limit_templates
(
    id                       SERIAL PRIMARY KEY,
    card_type_id             INTEGER        NOT NULL,
    daily_limit              DECIMAL(15, 2) NOT NULL,
    monthly_limit            DECIMAL(15, 2) NOT NULL,
    single_transaction_limit DECIMAL(15, 2) NOT NULL,
    atm_daily_limit          DECIMAL(15, 2) NOT NULL,
    currency                 VARCHAR(3)     NOT NULL DEFAULT 'RUB',
    is_default               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_limit_template_card_type FOREIGN KEY (card_type_id)
        REFERENCES card_types (id) ON DELETE CASCADE,
    CONSTRAINT check_template_daily_limit_positive CHECK (daily_limit > 0),
    CONSTRAINT check_template_monthly_limit_positive CHECK (monthly_limit > 0),
    CONSTRAINT check_template_single_limit_positive CHECK (single_transaction_limit > 0),
    CONSTRAINT check_template_atm_limit_positive CHECK (atm_daily_limit >= 0),
    CONSTRAINT check_template_daily_less_monthly CHECK (daily_limit <= monthly_limit),
    CONSTRAINT check_template_single_less_daily CHECK (single_transaction_limit <= daily_limit),
    CONSTRAINT check_template_currency_length CHECK (LENGTH(currency) = 3)
);

COMMENT ON TABLE card_type_limit_templates IS 'Шаблоны лимитов для типов карт';

CREATE INDEX idx_card_type_limit_templates_card_type ON card_type_limit_templates (card_type_id);
CREATE INDEX idx_card_type_limit_templates_is_default ON card_type_limit_templates (card_type_id, is_default);

-- Таблица: card_limit_history (История изменений лимитов карт)
CREATE TABLE card_limit_history
(
    id                           SERIAL PRIMARY KEY,
    card_limit_id                INTEGER        NOT NULL,
    card_id                      INTEGER        NOT NULL,
    old_daily_limit              DECIMAL(15, 2),
    new_daily_limit              DECIMAL(15, 2),
    old_monthly_limit            DECIMAL(15, 2),
    new_monthly_limit            DECIMAL(15, 2),
    old_single_transaction_limit DECIMAL(15, 2),
    new_single_transaction_limit DECIMAL(15, 2),
    old_atm_daily_limit          DECIMAL(15, 2),
    new_atm_daily_limit          DECIMAL(15, 2),
    changed_by                   INTEGER        NOT NULL,
    change_reason                TEXT,
    changed_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_limit_history_card_limit FOREIGN KEY (card_limit_id)
        REFERENCES card_limits (id) ON DELETE CASCADE,
    CONSTRAINT fk_limit_history_card FOREIGN KEY (card_id)
        REFERENCES cards (id) ON DELETE CASCADE,
    CONSTRAINT fk_limit_history_changed_by FOREIGN KEY (changed_by)
        REFERENCES employees (id) ON DELETE RESTRICT
);

COMMENT ON TABLE card_limit_history IS 'История изменений лимитов карт (аудит)';

CREATE INDEX idx_card_limit_history_card_limit_id ON card_limit_history (card_limit_id);
CREATE INDEX idx_card_limit_history_card_id ON card_limit_history (card_id);
CREATE INDEX idx_card_limit_history_changed_at ON card_limit_history (changed_at);

-- =====================================================
-- 4. ТАБЛИЦЫ ДОКУМЕНТОВ И ИСТОРИИ
-- =====================================================

-- Таблица: documents (Документы к заявкам)
-- 🔗 СВЯЗЬ: request_id → card_requests(id) ⚠️ ВАЖНАЯ СВЯЗЬ
CREATE TABLE documents
(
    id            SERIAL PRIMARY KEY,
    request_id    INTEGER      NOT NULL,  -- ⚠️ Связь с card_requests
    document_type VARCHAR(50)  NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    file_path     TEXT         NOT NULL,
    file_size     INTEGER      NOT NULL,
    uploaded_by   INTEGER      NOT NULL,
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- ⚠️ ВАЖНАЯ СВЯЗЬ #3: documents → card_requests
    CONSTRAINT fk_document_request FOREIGN KEY (request_id)
        REFERENCES card_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_uploaded_by FOREIGN KEY (uploaded_by)
        REFERENCES employees (id) ON DELETE RESTRICT,
    CONSTRAINT check_document_type CHECK (document_type <> ''),
    CONSTRAINT check_file_name CHECK (file_name <> ''),
    CONSTRAINT check_file_path CHECK (file_path <> ''),
    CONSTRAINT check_file_size_positive CHECK (file_size > 0)
);

COMMENT ON TABLE documents IS 'Скан-копии документов к заявкам';
COMMENT ON COLUMN documents.request_id IS '⚠️ Внешний ключ на card_requests - СВЯЗЬ УСТАНОВЛЕНА';

CREATE INDEX idx_documents_request_id ON documents (request_id);

-- Таблица: request_history (История изменений заявок)
-- 🔗 СВЯЗИ:
--   1. request_id → card_requests(id) ⚠️ ВАЖНАЯ СВЯЗЬ
--   2. old_status_id → request_statuses(id)
--   3. new_status_id → request_statuses(id)
--   4. changed_by → employees(id)
CREATE TABLE request_history
(
    id             SERIAL PRIMARY KEY,
    request_id     INTEGER NOT NULL,  -- ⚠️ Связь с card_requests
    old_status_id  INTEGER,
    new_status_id  INTEGER NOT NULL,
    changed_by     INTEGER NOT NULL,
    change_comment TEXT,
    changed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- ⚠️ ВАЖНАЯ СВЯЗЬ #4: request_history → card_requests
    CONSTRAINT fk_req_history_request FOREIGN KEY (request_id)
        REFERENCES card_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_req_history_old_status FOREIGN KEY (old_status_id)
        REFERENCES request_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_req_history_new_status FOREIGN KEY (new_status_id)
        REFERENCES request_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_req_history_changed_by FOREIGN KEY (changed_by)
        REFERENCES employees (id) ON DELETE RESTRICT
);

COMMENT ON TABLE request_history IS 'История изменений статусов заявок (аудит)';
COMMENT ON COLUMN request_history.request_id IS '⚠️ Внешний ключ на card_requests - СВЯЗЬ УСТАНОВЛЕНА';

CREATE INDEX idx_req_history_request_id ON request_history (request_id);
CREATE INDEX idx_req_history_changed_at ON request_history (changed_at);

-- Таблица: card_history (История изменений карт)
CREATE TABLE card_history
(
    id            SERIAL PRIMARY KEY,
    card_id       INTEGER NOT NULL,
    old_status_id INTEGER,
    new_status_id INTEGER NOT NULL,
    changed_by    INTEGER NOT NULL,
    change_reason TEXT,
    changed_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_history_card FOREIGN KEY (card_id)
        REFERENCES cards (id) ON DELETE CASCADE,
    CONSTRAINT fk_card_history_old_status FOREIGN KEY (old_status_id)
        REFERENCES card_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_history_new_status FOREIGN KEY (new_status_id)
        REFERENCES card_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_history_changed_by FOREIGN KEY (changed_by)
        REFERENCES employees (id) ON DELETE RESTRICT
);

COMMENT ON TABLE card_history IS 'История изменений статусов карт (аудит)';

CREATE INDEX idx_card_history_card_id ON card_history (card_id);
CREATE INDEX idx_card_history_changed_at ON card_history (changed_at);

-- Таблица: access_logs (Логи доступа)
CREATE TABLE access_logs
(
    id            SERIAL PRIMARY KEY,
    employee_id   INTEGER      NOT NULL,
    login_time    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    ip_address    VARCHAR(45)  NOT NULL,
    user_agent    TEXT,
    action        VARCHAR(100) NOT NULL,
    is_successful BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_access_log_employee FOREIGN KEY (employee_id)
        REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT check_ip_address CHECK (ip_address <> ''),
    CONSTRAINT check_action CHECK (action <> '')
    );

COMMENT ON TABLE access_logs IS 'Журнал входов сотрудников в систему';

CREATE INDEX idx_access_logs_employee_id ON access_logs (employee_id);
CREATE INDEX idx_access_logs_login_time ON access_logs (login_time);

-- =====================================================
-- ПРОВЕРКА СВЯЗЕЙ (VERIFICATION QUERIES)
-- =====================================================

-- Эти запросы помогут проверить, что все связи установлены правильно:

-- 1. Проверка связи: documents → card_requests
-- SELECT constraint_name, table_name, column_name 
-- FROM information_schema.key_column_usage 
-- WHERE table_name = 'documents' AND column_name = 'request_id';

-- 2. Проверка связи: card_requests → request_statuses
-- SELECT constraint_name, table_name, column_name 
-- FROM information_schema.key_column_usage 
-- WHERE table_name = 'card_requests' AND column_name = 'status_id';

-- 3. Проверка связи: request_history → card_requests
-- SELECT constraint_name, table_name, column_name 
-- FROM information_schema.key_column_usage 
-- WHERE table_name = 'request_history' AND column_name = 'request_id';

-- 4. Проверка связи: cards → card_statuses
-- SELECT constraint_name, table_name, column_name 
-- FROM information_schema.key_column_usage 
-- WHERE table_name = 'cards' AND column_name = 'status_id';

-- =====================================================
-- КОНЕЦ СКРИПТА
-- =====================================================