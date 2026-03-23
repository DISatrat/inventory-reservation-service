package org.example.inventory_reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.inventory_reservation_service.model.Reservation;

/**
 * DTO for {@link Reservation}
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ReservationDto {
    private Long productId;
    private int quantity;
}