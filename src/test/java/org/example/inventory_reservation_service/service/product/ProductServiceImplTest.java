package org.example.inventory_reservation_service.service.product;

import org.example.inventory_reservation_service.exception.EntityNotFoundException;
import org.example.inventory_reservation_service.exception.NotEnoughStockException;
import org.example.inventory_reservation_service.model.Product;
import org.example.inventory_reservation_service.repository.ProductDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testDecreaseStock_SuccessfulDecrement() {
        Long productId = 1L;
        int quantity = 2;

        Product product = new Product();
        product.setId(productId);
        product.setStock(5);
        product.setVersion(0L);

        when(productDao.findById(productId)).thenReturn(Optional.of(product));

        productService.decreaseStock(quantity, productId);

        assertEquals(3, product.getStock());
        verify(productDao).findById(productId);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void testDecreaseStock_NotEnoughStock() {
        Long productId = 1L;
        int quantity = 6;

        Product product = new Product();
        product.setId(productId);
        product.setStock(5);
        product.setVersion(0L);

        when(productDao.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(NotEnoughStockException.class, () ->
                productService.decreaseStock(quantity, productId)
        );

        assertEquals(5, product.getStock());
        verify(productDao).findById(productId);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void testDecreaseStock_ProductNotFound() {
        Long productId = 999L;
        int quantity = 1;

        when(productDao.findById(productId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                productService.decreaseStock(quantity, productId)
        );

        verify(productDao).findById(productId);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void testDecreaseStock_ExactlyAvailable() {
        Long productId = 1L;
        int quantity = 5;

        Product product = new Product();
        product.setId(productId);
        product.setStock(5);
        product.setVersion(0L);

        when(productDao.findById(productId)).thenReturn(Optional.of(product));

        productService.decreaseStock(quantity, productId);

        assertEquals(0, product.getStock());
        verify(productDao).findById(productId);
        verify(productDao, never()).save(any(Product.class));
    }

}