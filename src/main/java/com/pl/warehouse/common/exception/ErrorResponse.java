package com.pl.warehouse.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * STUDY — Consistent JSON error body for the frontend / API clients.
 *
 * <p>{@link GlobalExceptionHandler} always returns this shape so React can show
 * {@code message} (and optional {@code details} for field validation) without parsing HTML.
 *
 * <h2>Alternatives (say in interviews)</h2>
 * RFC 7807 Problem Details ({@code application/problem+json}), Spring Boot default
 * {@code /error} attributes, or GraphQL error envelopes.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponse(Instant.now(), status, error, message, path, details);
    }
}
