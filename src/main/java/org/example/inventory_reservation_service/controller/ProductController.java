package org.example.inventory_reservation_service.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventory_reservation_service.dto.ProductDto;
import org.example.inventory_reservation_service.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> productInfo(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.info(id));
    }

    @GetMapping("/top-reserved")
    public ResponseEntity<List<ProductDto>> topReservedProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.topReserver());
    }
}
