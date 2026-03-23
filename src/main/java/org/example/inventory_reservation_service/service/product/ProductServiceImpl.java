package org.example.inventory_reservation_service.service.product;

import lombok.RequiredArgsConstructor;
import org.example.inventory_reservation_service.dto.ProductDto;
import org.example.inventory_reservation_service.exception.EntityNotFoundException;
import org.example.inventory_reservation_service.exception.NotEnoughStockException;
import org.example.inventory_reservation_service.mapper.ProductMapper;
import org.example.inventory_reservation_service.model.Product;
import org.example.inventory_reservation_service.repository.ProductDao;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ProductMapper productMapper;

    @Override
    public ProductDto info(Long id) {
        Product product = get(id);
        return productMapper.toDto(product);
    }

    @Override
    public Product get(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Override
    public void decreaseStock(int quantity, Long productId) {
        Product product = get(productId);

        if (product.getStock() < quantity) {
            throw new NotEnoughStockException(
                    String.format("Not enough stock. Available: %d, Requested: %d",
                            product.getStock(), quantity)
            );
        }

        product.setStock(product.getStock() - quantity);
    }

    @Override
    public List<ProductDto> topReserver() {
        return productDao.findTopReserved(Instant.now().minus(24, ChronoUnit.HOURS), PageRequest.of(0,5))
                .stream()
                    .map(productMapper::toDto)
                    .collect(Collectors.toList());
    }
}
