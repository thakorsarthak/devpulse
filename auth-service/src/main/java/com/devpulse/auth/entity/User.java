package com.devpulse.auth.entity;

import io.micrometer.core.annotation.Counted;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


/**
 * User entity - maps to the users table created by Flyway V1.
 *
 * WHY ddl-auto=validate:
 * Flyway owns the schema. JPA only validates that the entity matches what Flyway created.
 * If they don't match, startup fails immediately — this catches schema drift early.
 *
 * WHY provider field:
 * Same user table handles both local (email/password) and GitHub OAuth users.
 * Provider field tells us which auth mechanism was used.
 * This is cleaner than separate tables.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // nullable - GiyHub OAuth users have no password
    @Column
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    //github OAuth fields
    @Column(name = "github_id" , unique = true)
    private String githubId;

    @Column(name = "github_username")
    private String githubUsername;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "email_verified" , nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
