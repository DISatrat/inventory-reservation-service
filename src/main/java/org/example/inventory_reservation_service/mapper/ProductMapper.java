package org.example.inventory_reservation_service.mapper;

import org.example.inventory_reservation_service.dto.ProductDto;
import org.example.inventory_reservation_service.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
}
