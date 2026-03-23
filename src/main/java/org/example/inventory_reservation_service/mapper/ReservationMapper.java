package org.example.inventory_reservation_service.mapper;

import org.example.inventory_reservation_service.dto.ReservationDto;
import org.example.inventory_reservation_service.model.Product;
import org.example.inventory_reservation_service.model.Reservation;
import org.example.inventory_reservation_service.model.enums.ReservationStatus;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    default Reservation toEntity(ReservationDto dto, Product product) {
        Reservation reservation = new Reservation();
        reservation.setQuantity(dto.getQuantity());
        reservation.setProduct(product);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCreatedAt(Instant.now());
        reservation.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        return reservation;
    }
}
