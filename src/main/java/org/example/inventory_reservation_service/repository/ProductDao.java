package org.example.inventory_reservation_service.repository;

import org.example.inventory_reservation_service.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProductDao extends JpaRepository<Product, Long> {

    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN Reservation r ON p.id = r.product.id " +
            "WHERE r.status = 'CONFIRMED' " +
            "AND r.createdAt >= :since " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(r.id) DESC")
    List<Product> findTopReserved(@Param("since") Instant since, Pageable pageable);
}
