package com.pl.warehouse.common.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * STUDY — Central place that maps exceptions → HTTP + {@link ErrorResponse} JSON.
 *
 * <h2>What is {@code @RestControllerAdvice}?</h2>
 * Spring applies this class to all {@code @RestController}s. When a handler method throws,
 * matching {@code @ExceptionHandler} methods run instead of a raw stack-trace page.
 * Controllers stay thin; services throw domain exceptions; this class owns the HTTP shape.
 *
 * <h2>Why bother?</h2>
 * The React UI (and any API client) needs stable JSON: status, message, path, optional details —
 * not Hibernate/SQL text. Translate DB uniqueness into a friendly 409 when possible.
 *
 * <h2>Flow</h2>
 * <pre>
 * Service throws ResourceNotFoundException
 *   → handleNotFound → 404 + ErrorResponse
 * Service throws ConflictException
 *   → handleConflict → 409 + ErrorResponse
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", ex.getMessage(), request.getRequestURI()));
    }

    /**
     * STUDY: {@code @Version} collision — another request updated the same inventory row.
     * Client should reload and retry (classic mid-level concurrency interview topic).
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "Conflict",
                        "Stock was updated by another request. Refresh and try again.",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String message = "Database constraint violated.";
        String raw = ex.getMostSpecificCause().getMessage();
        if (raw != null && raw.contains("uq_inventory_warehouse_item")) {
            message = "Stock already exists for this warehouse and item. "
                    + "Edit the existing row or use Save stock (upsert).";
        } else if (raw != null && raw.toLowerCase().contains("unique")) {
            message = "A unique value already exists (duplicate name/email/key).";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", message, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Validation Failed",
                        "Request body is invalid",
                        request.getRequestURI(),
                        details
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "Internal Server Error",
                        ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                        request.getRequestURI()
                ));
    }
}
