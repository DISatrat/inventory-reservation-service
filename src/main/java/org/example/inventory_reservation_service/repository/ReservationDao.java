package org.example.inventory_reservation_service.repository;

import org.example.inventory_reservation_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservationDao extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
                  "WHERE r.status = 'ACTIVE' " +
                  "AND r.expiresAt < :now")
    List<Reservation> findExpiredActiveReservations(@Param("now") Instant now);
}
