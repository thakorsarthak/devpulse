

-- V1: Initial schema for ingestion-service
-- Stores raw webhook events from github
-- We are storing the raw payload as JSONB for two reasons:
-- 1: GitHub payloads are large and semi-structured
-- 2: We can query specific fields using PostgreSQL JSONB operators
-- 3: If our parsing logic changes, raw data is always available


CREATE TABLE webhook_events(
    id                      BIGSERIAL PRIMARY KEY,
    event_type              VARCHAR(1OO) NOT NULL,
    repository_name         VARCHAR(255) NOT NULL,
    repository_full_name    VARCHAR(255) NOT NULL,
    sender_login            VARCHAR(255),
    delivery_id             VARCHAR(255) UNIQUE,
    raw_payload             JSONB NOT NULL,
    processed               BOOLEAN NOT NULL DEFAULT FALSE,
    kafka_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index for fast queries by repository
CREATE INDEX idx_webhook_events_repo
    ON webhook_events(repository_full_name);

-- Index for unprocessed events (background retry job can use this)
CREATE INDEX idx_webhook_events_unprocessed
    ON webhook_events(processed) WHERE processed = FALSE;


-- V1: Error logs table
-- Apps send error logs to ingestion-service for AI analysis
CREATE TABLE error_logs (
                            id              BIGSERIAL PRIMARY KEY,
                            service_name    VARCHAR(255) NOT NULL,
                            environment     VARCHAR(50) NOT NULL DEFAULT 'production',
                            error_message   TEXT NOT NULL,
                            stack_trace     TEXT,
                            severity        VARCHAR(50) NOT NULL DEFAULT 'ERROR',
                            resolved        BOOLEAN NOT NULL DEFAULT FALSE,
                            ai_analyzed     BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_error_logs_service
    ON error_logs(service_name);

CREATE INDEX idx_error_logs_unanalyzed
    ON error_logs(ai_analyzed) WHERE ai_analyzed = FALSE;