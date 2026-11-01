package com.pl.warehouse.common.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * STUDY — Stable JSON page wrapper for list endpoints.
 *
 * <h2>Why pagination?</h2>
 * Production APIs must not return unbounded lists (memory, latency, slow UI).
 * Clients ask for {@code page} + {@code size}; the server returns one slice plus totals
 * so the UI can build "Page 2 of 12" controls.
 *
 * <h2>What is this class?</h2>
 * Wraps Spring Data {@link Page} into a predictable record the frontend can deserialize.
 * Controllers call {@link #from(Page)} after the service returns a {@code Page<T>}.
 *
 * <h2>Interview tips</h2>
 * <ul>
 *   <li>{@code totalElements} = rows matching the query; {@code content.size()} = this page only</li>
 *   <li>Spring page index is <b>zero-based</b> ({@code page=0} is the first page)</li>
 *   <li>Alternatives: cursor/keyset pagination for huge tables; GraphQL connections</li>
 * </ul>
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
