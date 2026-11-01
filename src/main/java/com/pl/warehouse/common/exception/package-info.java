/**
 * STUDY — Shared API error model (exceptions → HTTP JSON).
 *
 * <p><b>What lives here:</b> domain exceptions, a uniform {@link ErrorResponse} body,
 * and {@link GlobalExceptionHandler} ({@code @RestControllerAdvice}) that maps them.
 *
 * <h2>Status-code map (learn this)</h2>
 * <ul>
 *   <li>{@link ResourceNotFoundException} → <b>404</b> (id does not exist)</li>
 *   <li>{@link ConflictException} → <b>409</b> (exists but cannot proceed: duplicate, business rule)</li>
 *   <li>Validation ({@code @Valid} failures) → <b>400</b></li>
 *   <li>Unexpected → <b>500</b> (prefer not to leak raw SQL)</li>
 * </ul>
 *
 * <p>Feature services throw these types; they do not build HTTP responses themselves.
 */
package com.pl.warehouse.common.exception;
