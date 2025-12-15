package com.cryptoArb.crypto_price_aggregator.config;

import com.cryptoArb.crypto_price_aggregator.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults()) // Enable CORS
                                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST APIs
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/**").permitAll() // Allow monitoring
                                                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                                                .requestMatchers("/frontend/**").permitAll() // Allow frontend static
                                                                                             // files
                                                .requestMatchers("/api/prices/**").permitAll() // Allow public price API
                                                                                               // access
                                                .requestMatchers("/api/arbitrage/**").permitAll() // Allow public
                                                                                                  // arbitrage API
                                                                                                  // access
                                                .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoints
                                                .requestMatchers("/login/**", "/oauth2/**").permitAll() // Allow OAuth2
                                                                                                        // endpoints
                                                .requestMatchers("/api/**").authenticated() // Secure other API
                                                                                            // endpoints
                                                .anyRequest().authenticated())
                                // .httpBasic(Customizer.withDefaults()) // Enable Basic Auth for API access
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/frontend/index.html") // Redirect to frontend login page
                                                .defaultSuccessUrl("/frontend/index.html", true) // Redirect after
                                                                                                 // successful login
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService) // Use custom
                                                                                                      // OAuth2 user
                                                                                                      // service
                                                ))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/frontend/index.html")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"))
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

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                                "https://pranay-a-1.github.io",
                                "http://localhost:8080",
                                "http://localhost:4040" // ngrok web interface
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
