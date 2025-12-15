package com.cryptoArb.crypto_price_aggregator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Handles OAuth2 login and user information endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    /**
     * Get current authenticated user information
     * 
     * @param principal The authenticated OAuth2 user
     * @return User information including GitHub profile data
     */
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("authenticated", true);
        userInfo.put("username", principal.getAttribute("login"));
        userInfo.put("name", principal.getAttribute("name"));
        userInfo.put("email", principal.getAttribute("email"));
        userInfo.put("avatarUrl", principal.getAttribute("avatar_url"));
        userInfo.put("githubId", principal.getAttribute("id"));
        userInfo.put("userId", principal.getAttribute("userId"));

        log.debug("Fetched authenticated user info for: {}", (String) principal.getAttribute("login"));
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Get OAuth2 login status
     * 
     * @param principal The authenticated OAuth2 user (may be null)
     * @return Login status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> status = new HashMap<>();
        status.put("authenticated", principal != null);
        if (principal != null) {
            status.put("username", principal.getAttribute("login"));
        }
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint to initiate logout
     * Note: Actual logout is handled by Spring Security
     * 
     * @return Logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout endpoint called");
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
