package com.devpulse.auth.entity;

/**
 * Authentication provider type.
 *
 * LOCAL  = registered with email + password
 * GITHUB = logged in via GitHub OAuth2
 *
 * Stored as string in DB (EnumType.STRING) so the value is human-readable in the database, not just a number.
 * This matters for debugging production issues.
 */
public enum AuthProvider {
    LOCAL,
    GITHUB
}