package com.cryptoArb.crypto_price_aggregator.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an OAuth2 authenticated user.
 * Stores user information from GitHub OAuth2 provider.
 */
@Entity
@Table(name = "oauth2_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique GitHub user ID
     */
    @Column(nullable = false, unique = true)
    private String githubId;

    /**
     * GitHub username
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * User's email from GitHub
     */
    @Column(nullable = true)
    private String email;

    /**
     * User's full name from GitHub
     */
    @Column(nullable = true)
    private String name;

    /**
     * GitHub avatar URL
     */
    @Column(nullable = true)
    private String avatarUrl;

    /**
     * User roles (comma-separated)
     */
    @Column(nullable = false)
    private String roles;

    /**
     * Account creation timestamp
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last login timestamp
     */
    @Column(nullable = false)
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastLogin = LocalDateTime.now();
    }
}
