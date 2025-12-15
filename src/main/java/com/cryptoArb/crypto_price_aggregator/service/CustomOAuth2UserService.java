package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.User;
import com.cryptoArb.crypto_price_aggregator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom OAuth2 User Service for handling GitHub OAuth2 authentication.
 * Loads or creates users from GitHub OAuth2 provider.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user from GitHub
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract user attributes from GitHub response
        Map<String, Object> attributes = oauth2User.getAttributes();

        String githubId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("avatar_url");

        log.info("GitHub OAuth2 login attempt for user: {} (GitHub ID: {})", username, githubId);

        // Find or create user
        User user = userRepository.findByGithubId(githubId)
                .map(existingUser -> {
                    // Update existing user info
                    existingUser.setUsername(username);
                    existingUser.setEmail(email);
                    existingUser.setName(name);
                    existingUser.setAvatarUrl(avatarUrl);
                    existingUser.setLastLogin(LocalDateTime.now());
                    log.info("Updated existing user: {}", username);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Create new user
                    User newUser = User.builder()
                            .githubId(githubId)
                            .username(username)
                            .email(email)
                            .name(name)
                            .avatarUrl(avatarUrl)
                            .roles("ROLE_USER")
                            .build();
                    log.info("Created new user: {}", username);
                    return userRepository.save(newUser);
                });

        // Add custom attributes including our database user ID
        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("userId", user.getId());
        modifiedAttributes.put("roles", user.getRoles());

        return new DefaultOAuth2User(
                Collections.singleton(() -> "ROLE_USER"),
                modifiedAttributes,
                "login" // GitHub uses "login" as the username attribute
        );
    }
}
