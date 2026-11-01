package com.pl.warehouse.config;

import org.springframework.context.annotation.Configuration;

/**
 * STUDY — CORS concepts for the warehouse SPA ↔ API setup.
 *
 * <h2>Why do browsers need CORS?</h2>
 * A page on {@code http://localhost:5173} calling {@code http://localhost:8080} is a
 * <b>cross-origin</b> request (different port = different origin). Browsers enforce the
 * Same-Origin Policy and only expose the response to JavaScript if the API sends the right
 * {@code Access-Control-*} headers (and answers preflight {@code OPTIONS} for non-simple calls).
 *
 * <p>curl / Postman / server-to-server clients do <b>not</b> enforce CORS — this is a browser rule.
 *
 * <h2>Where are the real rules in this project?</h2>
 * With Spring Security on the classpath, CORS is configured in {@link SecurityConfig}
 * ({@code CorsConfigurationSource} + {@code http.cors(...)}) so the filter chain applies
 * the same policy. This class is kept as a marker so you remember the concept belongs under config.
 *
 * <p>You can delete this file once you are comfortable reading CORS off SecurityConfig alone.
 */
@Configuration
public class CorsConfig {
}
