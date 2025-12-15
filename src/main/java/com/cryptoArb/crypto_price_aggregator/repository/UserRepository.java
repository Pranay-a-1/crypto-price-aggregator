package com.cryptoArb.crypto_price_aggregator.repository;

import com.cryptoArb.crypto_price_aggregator.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides methods to query OAuth2 authenticated users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by GitHub ID
     * 
     * @param githubId The GitHub user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByGithubId(String githubId);

    /**
     * Find user by username
     * 
     * @param username The GitHub username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given GitHub ID
     * 
     * @param githubId The GitHub user ID
     * @return true if user exists, false otherwise
     */
    boolean existsByGithubId(String githubId);
}
