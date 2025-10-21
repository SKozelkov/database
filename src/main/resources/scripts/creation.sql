
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
COMMENT ON COLUMN roles.id IS 'Первичный ключ';
COMMENT ON COLUMN roles.role_name IS 'Название роли (уникальное)';
COMMENT ON COLUMN roles.description IS 'Описание роли';

-- Таблица: application_statuses (Статусы заявок)
CREATE TABLE application_statuses
(
    id          SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT check_app_status_name_not_empty CHECK (status_name <> '')
);

COMMENT ON TABLE application_statuses IS 'Справочник статусов заявок на карты';
COMMENT ON COLUMN application_statuses.id IS 'Первичный ключ';
COMMENT ON COLUMN application_statuses.status_name IS 'Название статуса';

-- Таблица: card_statuses (Статусы карт)
CREATE TABLE card_statuses
(
    id          SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    CONSTRAINT check_card_status_name_not_empty CHECK (status_name <> '')
);

COMMENT ON TABLE card_statuses IS 'Справочник статусов выпущенных карт';
COMMENT ON COLUMN card_statuses.id IS 'Первичный ключ';
COMMENT ON COLUMN card_statuses.status_name IS 'Название статуса';

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
COMMENT ON COLUMN card_types.id IS 'Первичный ключ';
COMMENT ON COLUMN card_types.type_name IS 'Название типа карты';
COMMENT ON COLUMN card_types.annual_fee IS 'Стоимость годового обслуживания';
COMMENT ON COLUMN card_types.currency IS 'Валюта (RUB, USD, EUR)';

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
COMMENT ON COLUMN branches.id IS 'Первичный ключ';
COMMENT ON COLUMN branches.name IS 'Название отделения';
COMMENT ON COLUMN branches.is_active IS 'Флаг активности отделения';

-- =====================================================
-- 2. ТАБЛИЦЫ СОТРУДНИКОВ И ОРГАНИЗАЦИЙ
-- =====================================================

-- Таблица: employees (Сотрудники банка)
CREATE TABLE employees
(
    id            SERIAL PRIMARY KEY,
    role_id       INTEGER      NOT NULL,
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
    CONSTRAINT check_employee_first_name CHECK (first_name <> ''),
    CONSTRAINT check_employee_last_name CHECK (last_name <> ''),
    CONSTRAINT check_employee_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT check_employee_phone CHECK (phone <> '')
);

COMMENT ON TABLE employees IS 'Сотрудники банка, работающие в системе';
COMMENT ON COLUMN employees.role_id IS 'Внешний ключ на таблицу roles';
COMMENT ON COLUMN employees.password_hash IS 'Хеш пароля для входа в систему';
COMMENT ON COLUMN employees.is_active IS 'Активен ли сотрудник';

-- Индексы для employees
CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_employees_role_id ON employees (role_id);

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
COMMENT ON COLUMN organizations.inn IS 'ИНН организации (10 или 12 цифр)';
COMMENT ON COLUMN organizations.kpp IS 'КПП организации (9 цифр)';
COMMENT ON COLUMN organizations.director_name IS 'ФИО генерального директора';

-- Индекс для organizations
CREATE INDEX idx_organizations_inn ON organizations (inn);

-- Таблица: users (Сотрудники организаций)
CREATE TABLE users
(
    id                  SERIAL PRIMARY KEY,
    organization_id     INTEGER      NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    middle_name         VARCHAR(100),
    position            VARCHAR(100) NOT NULL,
    passport_series     VARCHAR(10)  NOT NULL,
    passport_number     VARCHAR(10)  NOT NULL,
    passport_issued_by  TEXT         NOT NULL,
    passport_issue_date DATE         NOT NULL,
    birth_date          DATE         NOT NULL,
    phone               VARCHAR(20)  NOT NULL,
    email               VARCHAR(100),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT check_user_first_name CHECK (first_name <> ''),
    CONSTRAINT check_user_last_name CHECK (last_name <> ''),
    CONSTRAINT check_user_position CHECK (position <> ''),
    CONSTRAINT check_passport_series CHECK (passport_series <> ''),
    CONSTRAINT check_passport_number CHECK (passport_number <> ''),
    CONSTRAINT check_birth_date_valid CHECK (birth_date < CURRENT_DATE),
    CONSTRAINT check_user_age CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'),
    CONSTRAINT check_user_email_format CHECK (email IS NULL OR
                                              email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

COMMENT ON TABLE users IS 'Сотрудники организаций (физические лица - владельцы карт)';
COMMENT ON COLUMN users.organization_id IS 'Внешний ключ на таблицу organizations';
COMMENT ON COLUMN users.position IS 'Должность в организации';
COMMENT ON COLUMN users.passport_series IS 'Серия паспорта';
COMMENT ON COLUMN users.passport_number IS 'Номер паспорта';

-- Индексы для users
CREATE INDEX idx_users_organization_id ON users (organization_id);
CREATE INDEX idx_users_passport ON users (passport_series, passport_number);

-- =====================================================
-- 3. ОСНОВНЫЕ ОПЕРАЦИОННЫЕ ТАБЛИЦЫ
-- =====================================================

-- Таблица: card_applications (Заявки на карты)
CREATE TABLE card_applications
(
    id                   SERIAL PRIMARY KEY,
    user_id              INTEGER     NOT NULL,
    organization_id      INTEGER     NOT NULL,
    card_type_id         INTEGER     NOT NULL,
    branch_id            INTEGER     NOT NULL,
    status_id            INTEGER     NOT NULL,
    assigned_employee_id INTEGER,
    application_number   VARCHAR(50) NOT NULL UNIQUE,
    comments             TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_card_type FOREIGN KEY (card_type_id)
        REFERENCES card_types (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_status FOREIGN KEY (status_id)
        REFERENCES application_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_employee FOREIGN KEY (assigned_employee_id)
        REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT check_application_number CHECK (application_number <> '')
);

COMMENT ON TABLE card_applications IS 'Заявки на выпуск корпоративных карт';
COMMENT ON COLUMN card_applications.application_number IS 'Уникальный номер заявки';
COMMENT ON COLUMN card_applications.assigned_employee_id IS 'Менеджер, назначенный на заявку';

-- Индексы для card_applications
CREATE INDEX idx_applications_user_id ON card_applications (user_id);
CREATE INDEX idx_applications_organization_id ON card_applications (organization_id);
CREATE INDEX idx_applications_status_id ON card_applications (status_id);
CREATE INDEX idx_applications_assigned_employee ON card_applications (assigned_employee_id);
CREATE INDEX idx_applications_created_at ON card_applications (created_at);

-- Таблица: cards (Выпущенные карты)
CREATE TABLE cards
(
    id              SERIAL PRIMARY KEY,
    application_id  INTEGER      NOT NULL UNIQUE,
    user_id         INTEGER      NOT NULL,
    card_type_id    INTEGER      NOT NULL,
    status_id       INTEGER      NOT NULL,
    card_number     VARCHAR(16)  NOT NULL UNIQUE,
    cardholder_name VARCHAR(100) NOT NULL,
    expiry_date     DATE         NOT NULL,
    issue_date      DATE         NOT NULL DEFAULT CURRENT_DATE,
    cvv_hash        VARCHAR(255) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_application FOREIGN KEY (application_id)
        REFERENCES card_applications (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_type FOREIGN KEY (card_type_id)
        REFERENCES card_types (id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_status FOREIGN KEY (status_id)
        REFERENCES card_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT check_card_number_length CHECK (LENGTH(card_number) = 16),
    CONSTRAINT check_card_number_digits CHECK (card_number ~ '^[0-9]{16}$'),
    CONSTRAINT check_cardholder_name CHECK (cardholder_name <> ''),
    CONSTRAINT check_expiry_date_future CHECK (expiry_date > issue_date)
);

COMMENT ON TABLE cards IS 'Выпущенные корпоративные карты';
COMMENT ON COLUMN cards.application_id IS 'Связь с заявкой (один к одному)';
COMMENT ON COLUMN cards.card_number IS 'Номер карты (16 цифр)';
COMMENT ON COLUMN cards.cvv_hash IS 'Хеш CVV-кода для безопасности';

-- Индексы для cards
CREATE INDEX idx_cards_user_id ON cards (user_id);
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
COMMENT ON COLUMN card_limits.daily_limit IS 'Дневной лимит расходов';
COMMENT ON COLUMN card_limits.monthly_limit IS 'Месячный лимит расходов';
COMMENT ON COLUMN card_limits.atm_daily_limit IS 'Дневной лимит снятия в банкомате';

-- Индекс для card_limits
CREATE INDEX idx_card_limits_card_id ON card_limits (card_id);

-- =====================================================
-- 4. ТАБЛИЦЫ ДОКУМЕНТОВ И ИСТОРИИ
-- =====================================================

-- Таблица: documents (Документы к заявкам)
CREATE TABLE documents
(
    id             SERIAL PRIMARY KEY,
    application_id INTEGER      NOT NULL,
    document_type  VARCHAR(50)  NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_path      TEXT         NOT NULL,
    file_size      INTEGER      NOT NULL,
    uploaded_by    INTEGER      NOT NULL,
    uploaded_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_application FOREIGN KEY (application_id)
        REFERENCES card_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_uploaded_by FOREIGN KEY (uploaded_by)
        REFERENCES employees (id) ON DELETE RESTRICT,
    CONSTRAINT check_document_type CHECK (document_type <> ''),
    CONSTRAINT check_file_name CHECK (file_name <> ''),
    CONSTRAINT check_file_path CHECK (file_path <> ''),
    CONSTRAINT check_file_size_positive CHECK (file_size > 0)
);

COMMENT ON TABLE documents IS 'Скан-копии документов к заявкам';
COMMENT ON COLUMN documents.document_type IS 'Тип документа (Паспорт, Доверенность и т.д.)';
COMMENT ON COLUMN documents.uploaded_by IS 'Сотрудник, загрузивший документ';

-- Индексы для documents
CREATE INDEX idx_documents_application_id ON documents (application_id);

-- Таблица: application_history (История изменений заявок)
CREATE TABLE application_history
(
    id             SERIAL PRIMARY KEY,
    application_id INTEGER NOT NULL,
    old_status_id  INTEGER,
    new_status_id  INTEGER NOT NULL,
    changed_by     INTEGER NOT NULL,
    change_comment TEXT,
    changed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_history_application FOREIGN KEY (application_id)
        REFERENCES card_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_history_old_status FOREIGN KEY (old_status_id)
        REFERENCES application_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_history_new_status FOREIGN KEY (new_status_id)
        REFERENCES application_statuses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_history_changed_by FOREIGN KEY (changed_by)
        REFERENCES employees (id) ON DELETE RESTRICT
);

COMMENT ON TABLE application_history IS 'История изменений статусов заявок (аудит)';
COMMENT ON COLUMN application_history.old_status_id IS 'Предыдущий статус (NULL для первой записи)';
COMMENT ON COLUMN application_history.changed_by IS 'Сотрудник, изменивший статус';

-- Индексы для application_history
CREATE INDEX idx_app_history_application_id ON application_history (application_id);
CREATE INDEX idx_app_history_changed_at ON application_history (changed_at);

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
COMMENT ON COLUMN card_history.old_status_id IS 'Предыдущий статус (NULL для первой записи)';
COMMENT ON COLUMN card_history.changed_by IS 'Сотрудник, изменивший статус';

-- Индексы для card_history
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
COMMENT ON COLUMN access_logs.ip_address IS 'IP-адрес (поддержка IPv4 и IPv6)';
COMMENT ON COLUMN access_logs.user_agent IS 'Информация о браузере/устройстве';
COMMENT ON COLUMN access_logs.is_successful IS 'Успешность входа';

-- Индексы для access_logs
CREATE INDEX idx_access_logs_employee_id ON access_logs (employee_id);
CREATE INDEX idx_access_logs_login_time ON access_logs (login_time);

-- =====================================================
-- КОНЕЦ СКРИПТА
-- =====================================================
