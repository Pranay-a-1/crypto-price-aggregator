package com.cryptoArb.crypto_price_aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST APIs
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/**").permitAll() // Allow monitoring
                                                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                                                .requestMatchers("/api/**").authenticated() // Secure API
                                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults()) // Enable Basic Auth
                                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 console

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
                UserDetails user = User.builder()
                                .username("user")
                                .password(passwordEncoder.encode("password"))
                                .roles("USER")
                                .build();

                UserDetails admin = User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin"))
                                .roles("ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(user, admin);
        }

        /*
         * OAuth2 Showcase (Deferred/Advanced)
         * 
         * To enable OAuth2, you would typically add:
         * 
         * .oauth2Login(oauth2 -> oauth2
         * .loginPage("/login/oauth2")
         * .userInfoEndpoint(userInfo -> userInfo
         * .userService(this.oauth2UserService())
         * )
         * )
         * 
         * And configure application.properties with:
         * spring.security.oauth2.client.registration.github.client-id=...
         * spring.security.oauth2.client.registration.github.client-secret=...
         */
}
