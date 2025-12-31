package com.cryptoArb.crypto_price_aggregator.service;

import com.cryptoArb.crypto_price_aggregator.domain.User;
import com.cryptoArb.crypto_price_aggregator.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("Should create new user when user does not exist")
    void shouldCreateNewUser() {
        // Given
        OAuth2User oauth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = Map.of(
                "id", "12345",
                "login", "testuser",
                "email", "test@example.com",
                "name", "Test User",
                "avatar_url", "http://avatar.url"
        );
        given(oauth2User.getAttributes()).willReturn(attributes);

        given(userRepository.findByGithubId("12345")).willReturn(Optional.empty());

        User savedUser = User.builder()
                .id(1L)
                .githubId("12345")
                .username("testuser")
                .roles("ROLE_USER")
                .build();
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        OAuth2User result = customOAuth2UserService.processUser(oauth2User);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAttributes()).containsEntry("userId", 1L);
        assertThat(result.getName()).isEqualTo("testuser");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should update existing user when user exists")
    void shouldUpdateExistingUser() {
        // Given
        OAuth2User oauth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = Map.of(
                "id", "12345",
                "login", "updateduser",
                "email", "updated@example.com",
                "name", "Updated User",
                "avatar_url", "http://new.avatar.url"
        );
        given(oauth2User.getAttributes()).willReturn(attributes);

        User existingUser = User.builder()
                .id(1L)
                .githubId("12345")
                .username("olduser")
                .roles("ROLE_USER")
                .build();
        given(userRepository.findByGithubId("12345")).willReturn(Optional.of(existingUser));

        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        OAuth2User result = customOAuth2UserService.processUser(oauth2User);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("updateduser");

        verify(userRepository).save(existingUser);
        assertThat(existingUser.getUsername()).isEqualTo("updateduser");
        assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(existingUser.getLastLogin()).isNotNull();
    }
}
