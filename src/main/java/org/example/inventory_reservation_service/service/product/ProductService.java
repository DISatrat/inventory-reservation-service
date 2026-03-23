package org.example.inventory_reservation_service.service.product;

import org.example.inventory_reservation_service.dto.ProductDto;
import org.example.inventory_reservation_service.model.Product;

import java.util.List;

public interface ProductService {
    ProductDto info(Long id);

    Product get(Long id);

    void decreaseStock(int quantity,Long productId);

    List<ProductDto> topReserver();
}
