CREATE SEQUENCE hibernate_sequence;

-- Based on Spring Session 4.0.1 docs
-- https://docs.spring.io/spring-session/reference/configuration/jdbc.html

CREATE TABLE spring_session
(
    primary_id            char(36) NOT NULL,
    session_id            char(36) NOT NULL,
    creation_time         bigint   NOT NULL,
    last_access_time      bigint   NOT NULL,
    max_inactive_interval int      NOT NULL,
    expiry_time           bigint   NOT NULL,
    principal_name        varchar(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON spring_session (session_id);
CREATE INDEX spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE spring_session_attributes
(
    session_primary_id char(36)     NOT NULL,
    attribute_name     varchar(200) NOT NULL,
    attribute_bytes    bytea        NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES spring_session (primary_id) ON DELETE CASCADE
);

-- CAS client specific relation

CREATE TABLE cas_client_session (
    mapping_id text PRIMARY KEY,
    session_id text NOT NULL UNIQUE
);

-- Flyway schema

CREATE TABLE schema_table_omss
(
    installed_rank integer                 NOT NULL
        CONSTRAINT schema_table_omss_pk
            PRIMARY KEY,
    version        varchar(50),
    description    varchar(200)            NOT NULL,
    type           varchar(20)             NOT NULL,
    script         varchar(1000)           NOT NULL,
    checksum       integer,
    installed_by   varchar(100)            NOT NULL,
    installed_on   timestamp DEFAULT now() NOT NULL,
    execution_time integer                 NOT NULL,
    success        boolean                 NOT NULL
);

CREATE INDEX schema_table_omss_s_idx
    ON schema_table_omss (success);