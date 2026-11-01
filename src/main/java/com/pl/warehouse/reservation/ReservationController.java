package com.pl.warehouse.reservation;

import com.pl.warehouse.reservation.dto.ReservationRequest;
import com.pl.warehouse.reservation.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Soft allocation API.
 *
 * <pre>
 * GET  /api/v1/reservations?status=ACTIVE
 * POST /api/v1/reservations
 * POST /api/v1/reservations/{id}/release
 * POST /api/v1/reservations/{id}/fulfill
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) ReservationStatus status) {
        return reservationService.list(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return reservationService.create(request);
    }

    @PostMapping("/{id}/release")
    public ReservationResponse release(@PathVariable UUID id) {
        return reservationService.release(id);
    }

    @PostMapping("/{id}/fulfill")
    public ReservationResponse fulfill(@PathVariable UUID id) {
        return reservationService.fulfill(id);
    }
}
