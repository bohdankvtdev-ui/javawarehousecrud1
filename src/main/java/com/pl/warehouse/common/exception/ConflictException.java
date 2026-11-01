package com.pl.warehouse.common.exception;

/**
 * STUDY — Business conflict → HTTP <b>409</b> via {@link GlobalExceptionHandler}.
 *
 * <h2>When to throw this (not 404)</h2>
 * The resource or rule is understood, but the operation cannot proceed:
 * duplicate name/SKU, unique constraint you checked in the service, "already exists" upserts.
 *
 * <p><b>404</b> ({@link ResourceNotFoundException}) = "this id does not exist".
 * <b>409</b> = "it exists / conflicts with current state — try a different action".
 *
 * <p>Prefer throwing this with a clear message over letting the DB surface raw SQL to the client.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
