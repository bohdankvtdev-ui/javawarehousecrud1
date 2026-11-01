/**
 * STUDY — Cross-cutting Spring configuration (Security, CORS).
 *
 * <p><b>What lives here:</b> beans that affect every request — not feature business rules.
 * Keep "can this item be discontinued?" in services; keep "who may DELETE /api/**?" here.
 *
 * <h2>Read in this order</h2>
 * <ol>
 *   <li>{@link com.pl.warehouse.config.SecurityConfig} — filter chain, roles, CSRF, Basic Auth</li>
 *   <li>{@link com.pl.warehouse.config.CorsConfig} — why browsers need CORS (concepts; rules live in Security)</li>
 * </ol>
 *
 * <p>Wired because {@code @SpringBootApplication} component-scans {@code com.pl.warehouse.config}.
 */
package com.pl.warehouse.config;
