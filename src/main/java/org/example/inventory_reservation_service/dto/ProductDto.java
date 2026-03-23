package org.example.inventory_reservation_service.dto;

import lombok.*;
import org.example.inventory_reservation_service.model.Product;

/**
 * DTO for {@link Product}
 */

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProductDto {
    private final Long id;

    private String name;

    private int stock;

}