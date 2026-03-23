package org.example.inventory_reservation_service.service.reservation;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationMapper reservationMapper;
    private final ProductService productService;

    @Override
    public Long reserve(ReservationDto reservationDto) {
        cleanupExpiredReservations();
        Product product = productService.get(reservationDto.getProductId());

        if (product.getStock() < reservationDto.getQuantity()) {
            throw new NotEnoughStockException(
                    String.format("Not enough stock. Available: %d, Requested: %d",
                            product.getStock(), reservationDto.getQuantity())
            );
        }

        Reservation reservation = reservationMapper.toEntity(reservationDto, product);
        return reservationDao.save(reservation).getId();
    }

    @Override
    public Reservation get(Long id){
        return reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
    }

    public void cleanupExpiredReservations() {
        List<Reservation> expiredReservations = reservationDao.findExpiredActiveReservations(Instant.now());

        if (!expiredReservations.isEmpty()) {
            expiredReservations.forEach(reservation -> {
                reservation.setStatus(ReservationStatus.EXPIRED);
            });
            reservationDao.saveAll(expiredReservations);
        }
    }

    @Override
    @Transactional
    public void confirm(Long reservationId) {
        cleanupExpiredReservations();
        Reservation reservation = get(reservationId);

        if (reservation.getExpiresAt().isBefore(Instant.now())) {
            throw new ReservationExpiredException("Reservation has expired");
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidReservationStatusException(
                    "Reservation cannot be confirmed. Current status: " + reservation.getStatus()
            );
        }
        productService.decreaseStock(reservation.getQuantity(), reservation.getProduct().getId());
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }
}
