
-------------------------------------------
-- V1: Analytics schema
-- Stores aggregated metric per repository
-- ----------------------------------------

CREATE TABLE repository_metrics (

    id                      BIGSERIAL PRIMARY KEY ,
    repository_full_name    VARCHAR(255) NOT NULL UNIQUE ,
    repository_name         VARCHAR(255) NOT NULL ,
    total_commits           INTEGER NOT NULL DEFAULT 0,
    total_pushes            INTEGER NOT NULL DEFAULT 0,
    active_contributors     INTEGER NOT NULL DEFAULT 0,
    last_push_at            TIMESTAMP,
    last_pushed_by          VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_repo_metrics_full_name ON repository_metrics(repository_full_name);

-----------------------------------------------------------------------
-- Tracks individual push events for time-series queries
-- why SEPARATE TABLE :
-- repository_metrics = aggregated table (fast reads) kind of scoreboard
-- ===== it update after Calculate once when the event arrives and store the result
--  push_activity = raw events (historical queries
--------------------------------------------------------------------------

CREATE TABLE  push_activity (
    id                      BIGSERIAL PRIMARY KEY ,
    repository_full_name    VARCHAR(255) NOT NULL ,
    sender_login            VARCHAR(255),
    branch                  VARCHAR(255),
    commit_count            INTEGER NOT NULL DEFAULT 0,
    event_id                BIGINT,
    pushed_at               TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_push_activity_repo ON push_activity(repository_full_name);

CREATE INDEX  idx_push_activity_pushed_at ON push_activity(pushed_at);