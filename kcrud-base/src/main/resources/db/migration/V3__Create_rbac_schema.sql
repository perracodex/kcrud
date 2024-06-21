/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

-- noinspection SqlDialectInspectionForFile
-- https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rbac_role (
    role_id UUID,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(512) NULL,
    is_super BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_role_id PRIMARY KEY (role_id)
);

ALTER TABLE rbac_role
    ADD CONSTRAINT uq_rbac_role__role_name
        UNIQUE (role_name);

CREATE TRIGGER IF NOT EXISTS tg_rbac_role__updated_at
BEFORE UPDATE ON rbac_role
FOR EACH ROW
CALL 'UpdateTimestampTrigger';

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rbac_scope_rule (
    scope_rule_id UUID,
    role_id UUID NOT NULL,
    scope INTEGER NOT NULL,
    access_level INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_scope_rule_id PRIMARY KEY (scope_rule_id),

    CONSTRAINT fk_rbac_scope_rule__role_id FOREIGN KEY (role_id)
        REFERENCES rbac_role(role_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE rbac_scope_rule
    ADD CONSTRAINT uq_rbac_scope_rule__role_id__scope
        UNIQUE (role_id, scope);

CREATE TRIGGER IF NOT EXISTS tg_rbac_scope_rule__updated_at
BEFORE UPDATE ON rbac_scope_rule
FOR EACH ROW
CALL 'UpdateTimestampTrigger';

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rbac_field_rule (
    field_rule_id UUID,
    scope_rule_id UUID NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    access_level INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_field_rule_id PRIMARY KEY (field_rule_id),

    CONSTRAINT fk_rbac_field_rule__scope_rule_id FOREIGN KEY (scope_rule_id)
        REFERENCES rbac_scope_rule(scope_rule_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE rbac_field_rule
    ADD CONSTRAINT uq_rbac_field_rule__scope_rule_id__field_name
        UNIQUE (scope_rule_id, field_name);

CREATE TRIGGER IF NOT EXISTS tg_rbac_field_rule__updated_at
BEFORE UPDATE ON rbac_field_rule
FOR EACH ROW
CALL 'UpdateTimestampTrigger';
