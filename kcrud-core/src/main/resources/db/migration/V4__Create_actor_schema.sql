/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

-- noinspection SqlDialectInspectionForFile
-- https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS actor (
    actor_id UUID,
    username VARCHAR(16) NOT NULL,
    password VARCHAR(128) NOT NULL,
    role_id UUID NOT NULL,
    is_locked BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_actor_id PRIMARY KEY (actor_id),

    CONSTRAINT fk_rbac_role__role_id FOREIGN KEY (role_id)
        REFERENCES rbac_role(role_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

ALTER TABLE actor
    ADD CONSTRAINT uq_actor__username
        UNIQUE (username);

CREATE TRIGGER IF NOT EXISTS tg_actor__updated_at
BEFORE UPDATE ON actor
FOR EACH ROW
CALL 'kcrud.core.database.utils.UpdateTimestampTrigger';
