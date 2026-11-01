package com.pl.warehouse.common.exception;

/**
 * STUDY — Missing resource → HTTP <b>404</b> via {@link GlobalExceptionHandler}.
 *
 * <h2>When to throw this (not 409)</h2>
 * A lookup by id (or key) found nothing: item, warehouse, inventory row, etc.
 * The client asked for something that is not there.
 *
 * <p><b>404</b> = absent. <b>409</b> ({@link ConflictException}) = present but conflicting
 * (duplicate create, business rule violation).
 *
 * <p>Prefer this over a bare {@code RuntimeException} so every "not found" becomes a clear status code.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
