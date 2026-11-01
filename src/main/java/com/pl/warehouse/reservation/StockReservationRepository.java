package com.pl.warehouse.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    @EntityGraph(attributePaths = {"warehouse", "item"})
    List<StockReservation> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"warehouse", "item"})
    List<StockReservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);
}
