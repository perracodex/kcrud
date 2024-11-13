/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

-- noinspection SqlDialectInspectionForFile
-- https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS employee (
    employee_id UUID,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    work_email VARCHAR(256) NOT NULL,
    dob DATE NOT NULL,
    marital_status VARCHAR(64) NOT NULL,
    honorific_id INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_employee_id
        PRIMARY KEY (employee_id)
);

CREATE INDEX IF NOT EXISTS ix_employee__first_name
    ON employee (first_name);

CREATE INDEX IF NOT EXISTS ix_employee__last_name
    ON employee (last_name);

ALTER TABLE employee
    ADD CONSTRAINT uq_employee__work_email
    UNIQUE (work_email);

CREATE TRIGGER IF NOT EXISTS tg_employee__updated_at
    BEFORE UPDATE ON employee
    FOR EACH ROW CALL 'kcrud.core.database.util.UpdateTimestampTrigger';

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS contact (
    contact_id UUID,
    employee_id UUID NOT NULL,
    email VARCHAR(256) NOT NULL,
    phone VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_contact_id
        PRIMARY KEY (contact_id),

    CONSTRAINT fk_contact__employee_id
        FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_contact__employee_id
    ON contact (employee_id);

CREATE INDEX IF NOT EXISTS ix_contact__email
    ON contact (email);

CREATE INDEX IF NOT EXISTS ix_contact__phone
    ON contact (phone);

CREATE TRIGGER IF NOT EXISTS tg_contact__updated_at
    BEFORE UPDATE ON contact
    FOR EACH ROW CALL 'kcrud.core.database.util.UpdateTimestampTrigger';
