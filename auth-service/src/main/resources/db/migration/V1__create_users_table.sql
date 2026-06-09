-- V1 : Initial Schema for auth-service
-- Creates user table with role-based access
-- ---------------------------------------------------

CREATE TABLE users(

    id              BIGSERIAL PRIMARY KEY ,
    email           VARCHAR(255) NOT NULL  UNIQUE ,
    password        VARCHAR(255),
    name            VARCHAR(255) NOT NULL ,
    role            VARCHAR(255) NOT NULL DEFAULT 'ROLE_USER',

    -- Github OAuth fields
    -- NULL if user registered with email/password
    -- Populated when user logs via GitHub
    github_id       VARCHAR(255) UNIQUE ,
    github_username VARCHAR(255),
    avatar_url      VARCHAR(500),

    -- Auth provider tracking
    -- LOCAL = email/password, GITHUB = OAuth
    provider        VARCHAR(255) NOT NULL DEFAULT 'LOCAL',


    -- Account status
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit fields
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index on email for fast login lookups
CREATE  INDEX idx_unsers_email ON users(email);

-- Index for github_id for fast OAuth lookups
CREATE INDEX idx_users_github_id ON users(github_id);

COMMENT ON TABLE users IS 'DevPulse user accounts - supports local and GitHub OAuth authentication';