package org.example.inventory_reservation_service.service.reservation;

import org.example.inventory_reservation_service.dto.ReservationDto;
import org.example.inventory_reservation_service.exception.EntityNotFoundException;
import org.example.inventory_reservation_service.exception.InvalidReservationStatusException;
import org.example.inventory_reservation_service.exception.NotEnoughStockException;
import org.example.inventory_reservation_service.exception.ReservationExpiredException;
import org.example.inventory_reservation_service.mapper.ReservationMapper;
import org.example.inventory_reservation_service.model.Product;
import org.example.inventory_reservation_service.model.Reservation;
import org.example.inventory_reservation_service.model.enums.ReservationStatus;
import org.example.inventory_reservation_service.repository.ReservationDao;
import org.example.inventory_reservation_service.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ProductService productService;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void testReserve_SuccessfulReservation() {
        Long productId = 1L;
        int quantity = 2;
        ReservationDto reservationDTO = new ReservationDto(productId, quantity);

        Product product = new Product();
        product.setId(productId);
        product.setStock(5);

        Reservation reservation = new Reservation();
        reservation.setId(1L);

        when(productService.get(productId)).thenReturn(product);
        when(reservationMapper.toEntity(reservationDTO, product)).thenReturn(reservation);
        when(reservationDao.save(reservation)).thenReturn(reservation);

        Long result = reservationService.reserve(reservationDTO);

        assertNotNull(result);
        verify(productService).get(productId);
        verify(reservationMapper).toEntity(reservationDTO, product);
        verify(reservationDao).save(reservation);
        verify(productService, never()).decreaseStock(anyInt(), anyLong());
    }

    @Test
    void testReserve_NotEnoughStock() {
        Long productId = 1L;
        int quantity = 6;
        ReservationDto reservationDTO = new ReservationDto(productId, quantity);

        Product product = new Product();
        product.setId(productId);
        product.setStock(5);

        when(productService.get(productId)).thenReturn(product);

        assertThrows(NotEnoughStockException.class, () ->
                reservationService.reserve(reservationDTO)
        );

        verify(productService).get(productId);
        verify(reservationMapper, never()).toEntity(any(), any());
        verify(reservationDao, never()).save(any());
    }

    @Test
    void testConfirm_ReservationExpired() {
        Long reservationId = 1L;

        Product product = new Product();
        product.setId(1L);
        product.setStock(5);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setProduct(product);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setExpiresAt(Instant.now().minus(10, ChronoUnit.MINUTES));

        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationExpiredException.class, () ->
                reservationService.confirm(reservationId)
        );

        verify(reservationDao).findById(reservationId);
        verify(productService, never()).decreaseStock(anyInt(), anyLong());
    }

    @Test
    void testConfirm_InvalidStatus() {
        Long reservationId = 1L;

        Product product = new Product();
        product.setId(1L);
        product.setStock(5);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setProduct(product);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStatusException.class, () ->
                reservationService.confirm(reservationId)
        );

        verify(reservationDao).findById(reservationId);
        verify(productService, never()).decreaseStock(anyInt(), anyLong());
    }

    @Test
    void testConfirm_SuccessfulConfirmation() {
        Long reservationId = 1L;
        int quantity = 2;

        Product product = new Product();
        product.setId(1L);
        product.setStock(5);
        product.setVersion(0L);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setProduct(product);
        reservation.setQuantity(quantity);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        when(reservationDao.findExpiredActiveReservations(any(Instant.class))).thenReturn(Collections.emptyList());

        doNothing().when(productService).decreaseStock(quantity, product.getId());

        reservationService.confirm(reservationId);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        verify(reservationDao).findExpiredActiveReservations(any(Instant.class));
        verify(reservationDao).findById(reservationId);
        verify(productService).decreaseStock(quantity, product.getId());

        verify(reservationDao, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirm_ReservationNotFound() {
        Long reservationId = 999L;

        when(reservationDao.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reservationService.confirm(reservationId)
        );

        verify(reservationDao).findById(reservationId);
        verify(productService, never()).decreaseStock(anyInt(), anyLong());
    }
}