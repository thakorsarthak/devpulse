-- -------------------------------------------------------------
-- V2: Service registrations
-- Links a monitored service/repo to a DevPulse user
-- -------------------------------------------------------------
CREATE TABLE service_registrations (
                                       id              BIGSERIAL PRIMARY KEY,
                                       user_id         BIGINT NOT NULL REFERENCES users(id),
                                       service_name    VARCHAR(255) NOT NULL,
                                       environment     VARCHAR(50) NOT NULL DEFAULT 'production',
                                       created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                       UNIQUE(user_id, service_name)
);

CREATE INDEX idx_service_reg_service_name
    ON service_registrations(service_name);