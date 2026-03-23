package org.example.inventory_reservation_service.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventory_reservation_service.dto.ReservationDto;
import org.example.inventory_reservation_service.service.reservation.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping()
    public ResponseEntity<Long> createReservation(@RequestBody ReservationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserve(dto));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable("id") Long reservationId){
        reservationService.confirm(reservationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

