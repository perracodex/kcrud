/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

-- noinspection SqlDialectInspectionForFile
-- https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS scheduler_audit (
    audit_id UUID,
    task_name VARCHAR(200) NOT NULL,
    task_group VARCHAR(200) NOT NULL,
    fire_time TIMESTAMP WITH TIME ZONE NOT NULL,
    run_time LONG NOT NULL,
    outcome VARCHAR(64) NOT NULL,
    log TEXT,
    detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_id PRIMARY KEY (audit_id)
);

CREATE TRIGGER IF NOT EXISTS tg_scheduler_audit__updated_at
BEFORE UPDATE ON scheduler_audit
FOR EACH ROW
CALL 'UpdateTimestampTrigger';
