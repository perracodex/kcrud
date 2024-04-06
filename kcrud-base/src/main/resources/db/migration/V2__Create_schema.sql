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

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rbac_resource_rule (
    resource_rule_id UUID,
    role_id UUID NOT NULL,
    resource INTEGER NOT NULL,
    access_level INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_resource_rule_id PRIMARY KEY (resource_rule_id),

    CONSTRAINT fk_rbac_resource_rule__role_id FOREIGN KEY (role_id)
        REFERENCES rbac_role(role_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE rbac_resource_rule
    ADD CONSTRAINT uq_rbac_resource_rule__role_id__resource
        UNIQUE (role_id, resource);

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS rbac_field_rule (
    field_rule_id UUID,
    resource_rule_id UUID NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    access_level INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_field_rule_id PRIMARY KEY (field_rule_id),

    CONSTRAINT fk_rbac_field_rule__resource_rule_id FOREIGN KEY (resource_rule_id)
        REFERENCES rbac_resource_rule(resource_rule_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE rbac_field_rule
    ADD CONSTRAINT uq_rbac_field_rule__resource_rule_id__field_name
        UNIQUE (resource_rule_id, field_name);

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

ALTER TABLE actor ADD CONSTRAINT uq_actor__username UNIQUE (username);

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS employee (
    employee_id UUID,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    dob DATE NOT NULL,
    marital_status VARCHAR(64) NOT NULL,
    honorific INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_employee_id PRIMARY KEY (employee_id)
);

CREATE INDEX IF NOT EXISTS ix_employee__first_name ON employee (first_name);
CREATE INDEX IF NOT EXISTS ix_employee__last_name ON employee (last_name);

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS contact (
    contact_id UUID,
    employee_id UUID NOT NULL,
    email VARCHAR(256) NOT NULL,
    phone VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_contact_id PRIMARY KEY (contact_id),

    CONSTRAINT fk_contact__employee_id FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_contact__employee_id ON contact (employee_id);

-------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS employment (
    employment_id UUID,
    employee_id UUID NOT NULL,
    status INTEGER NOT NULL,
    probation_end_date DATE NULL,
    work_modality INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    comments VARCHAR(512) NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_employment_id PRIMARY KEY (employment_id),

    CONSTRAINT fk_employment__employee_id FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id) ON DELETE CASCADE ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_employment__employee_id ON employment (employee_id);

-------------------------------------------------------------------------------------
