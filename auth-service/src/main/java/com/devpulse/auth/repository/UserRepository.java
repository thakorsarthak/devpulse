package com.devpulse.auth.repository;

import com.devpulse.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User repository.
 *
 * Spring Data JPA generates all SQL at startup — no boilerplate needed.
 * Method names are parsed into SQL automatically:
 * findByEmail → SELECT * FROM users WHERE email = ?
 * findByGithubId → SELECT * FROM users WHERE github_id = ?
 * existsByEmail → SELECT COUNT(*) FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGithubId(String githubId);

    boolean existsByEmail(String email);
}
