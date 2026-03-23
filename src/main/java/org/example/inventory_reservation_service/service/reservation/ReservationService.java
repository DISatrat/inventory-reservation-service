package org.example.inventory_reservation_service.service.reservation;

import org.example.inventory_reservation_service.dto.ReservationDto;
import org.example.inventory_reservation_service.model.Reservation;

public interface ReservationService {

    Long reserve(ReservationDto reservationDTO);

    void confirm(Long reservationId);

    Reservation get(Long id);

    void cleanupExpiredReservations();
}
