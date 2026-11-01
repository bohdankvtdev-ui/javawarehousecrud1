package com.pl.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * STUDY / JOB-READY — Spring Security filter chain for this warehouse API.
 *
 * <h2>What is a {@link SecurityFilterChain}?</h2>
 * A ordered list of servlet filters in front of every request. Before your controller runs,
 * Security decides: allow, challenge with Basic Auth, or reject (401/403).
 *
 * <h2>What this study setup does</h2>
 * <ul>
 *   <li><b>Stateless</b> — no HTTP session; each request carries credentials (API-friendly)</li>
 *   <li><b>HTTP Basic</b> — {@code Authorization: Basic base64(user:pass)} (simple for Postman / study UI)</li>
 *   <li><b>Roles</b> — USER may create/update; ADMIN may also DELETE; GET {@code /api/**} is public</li>
 *   <li><b>CSRF off</b> — CSRF tokens protect cookie-based browser sessions. Stateless Bearer/Basic APIs
 *       typically disable CSRF; turn it on if you use cookie sessions for a same-site form UI</li>
 * </ul>
 *
 * <h2>Basic Auth vs JWT (interview talking point)</h2>
 * <ul>
 *   <li><b>Basic</b> — credentials every request (or cached by browser). Fine for demos / internal tools</li>
 *   <li><b>JWT / OAuth2 Resource Server</b> — login once, send a signed token; scales for SPAs + microservices
 *       (API Gateway + Keycloak / Auth0 / Cognito). Next career step after this file</li>
 * </ul>
 *
 * <p>Also: method security {@code @PreAuthorize("hasRole('ADMIN')")} as an alternative to URL matchers.
 *
 * <h2>Demo users (study only — never ship these)</h2>
 * <pre>
 * user  / user   → ROLE_USER
 * admin / admin  → ROLE_ADMIN
 * </pre>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // STUDY: OpenAPI / Swagger UI (springdoc) — public for learning
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * STUDY: In-memory users are fine for demos. Production uses DB users / LDAP / IdP.
     * Delegating encoder supports prefixes like {@code {bcrypt}}; encode() hashes for storage.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user"))
                .roles("USER")
                .build();

        var admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * STUDY: CORS rules applied by the Security filter chain (see class {@link CorsConfig} for "why").
     * Browser UI on {@code localhost:5173} calling API on another origin needs these headers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/actuator/**", config);
        return source;
    }
}
