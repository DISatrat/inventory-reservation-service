package org.example.inventory_reservation_service.controller;

import org.example.inventory_reservation_service.dto.ReservationDto;
import org.example.inventory_reservation_service.exception.InvalidReservationStatusException;
import org.example.inventory_reservation_service.exception.ReservationExpiredException;
import org.example.inventory_reservation_service.service.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @Test
    void testReserve_SuccessfulReservation() {
        Long productId = 1L;
        int quantity = 2;
        Long reservationId = 100L;
        ReservationDto reservationDTO = new ReservationDto(productId, quantity);

        when(reservationService.reserve(reservationDTO)).thenReturn(reservationId);

        ResponseEntity<?> response = reservationController.createReservation(reservationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(reservationId, response.getBody());
        verify(reservationService, times(1)).reserve(reservationDTO);
    }

    @Test
    void testConfirm_SuccessfulConfirmation() {
        Long reservationId = 1L;
        doNothing().when(reservationService).confirm(reservationId);

        ResponseEntity<?> response = reservationController.confirm(reservationId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reservationService, times(1)).confirm(reservationId);
    }

    @Test
    void testConfirm_ReservationExpired() {
        Long reservationId = 1L;
        doThrow(new ReservationExpiredException("Reservation has expired"))
                .when(reservationService).confirm(reservationId);

        assertThrows(ReservationExpiredException.class, () ->
                reservationController.confirm(reservationId)
        );
        verify(reservationService, times(1)).confirm(reservationId);
    }

    @Test
    void testConfirm_InvalidStatus() {
        Long reservationId = 1L;
        doThrow(new InvalidReservationStatusException("Reservation cannot be confirmed. Current status: CONFIRMED"))
                .when(reservationService).confirm(reservationId);

        assertThrows(InvalidReservationStatusException.class, () ->
                reservationController.confirm(reservationId)
        );
        verify(reservationService, times(1)).confirm(reservationId);
    }
}