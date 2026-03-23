package org.example.inventory_reservation_service.controller;

import org.example.inventory_reservation_service.model.Product;
import org.example.inventory_reservation_service.model.Reservation;
import org.example.inventory_reservation_service.model.enums.ReservationStatus;
import org.example.inventory_reservation_service.repository.ProductDao;
import org.example.inventory_reservation_service.repository.ReservationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        reservationDao.deleteAll();
        productDao.deleteAll();
    }

    @Test
    void shouldReturnTop5ProductsByConfirmedReservationsInLast24Hours() throws Exception {
        Product product1 = createProduct("Product 1", 100);
        Product product2 = createProduct("Product 2", 100);
        Product product3 = createProduct("Product 3", 100);
        Product product4 = createProduct("Product 4", 100);
        Product product5 = createProduct("Product 5", 100);
        Product product6 = createProduct("Product 6", 100);

        for (int i = 0; i < 10; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 8; i++) {
            createReservation(product2, ReservationStatus.CONFIRMED, Instant.now().minus(2, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 5; i++) {
            createReservation(product3, ReservationStatus.CONFIRMED, Instant.now().minus(3, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 3; i++) {
            createReservation(product4, ReservationStatus.CONFIRMED, Instant.now().minus(4, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 2; i++) {
            createReservation(product5, ReservationStatus.CONFIRMED, Instant.now().minus(5, ChronoUnit.HOURS));
        }

        createReservation(product6, ReservationStatus.CONFIRMED, Instant.now().minus(6, ChronoUnit.HOURS));

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[2].name").value("Product 3"))
                .andExpect(jsonPath("$[3].name").value("Product 4"))
                .andExpect(jsonPath("$[4].name").value("Product 5"));
    }

    @Test
    void shouldConsiderOnlyConfirmedReservations() throws Exception {
        Product product1 = createProduct("Product 1", 100);
        Product product2 = createProduct("Product 2", 100);

        for (int i = 0; i < 5; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 10; i++) {
            createReservation(product2, ReservationStatus.ACTIVE, Instant.now().minus(1, ChronoUnit.HOURS));
        }

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    void shouldConsiderOnlyReservationsFromLast24Hours() throws Exception {
        Product product1 = createProduct("Product 1", 100);
        Product product2 = createProduct("Product 2", 100);

        for (int i = 0; i < 5; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(12, ChronoUnit.HOURS));
        }

        for (int i = 0; i < 10; i++) {
            createReservation(product2, ReservationStatus.CONFIRMED, Instant.now().minus(48, ChronoUnit.HOURS));
        }

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    void shouldReturnEmptyListWhenNoReservations() throws Exception {
        createProduct("Product 1", 100);
        createProduct("Product 2", 100);

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnLessThan5ProductsWhenNotEnoughProducts() throws Exception {
        Product product1 = createProduct("Product 1", 100);

        for (int i = 0; i < 3; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    void shouldReturnProductsOrderedByReservationCountDesc() throws Exception {
        Product product1 = createProduct("Most Popular", 100);
        Product product2 = createProduct("Medium Popular", 100);
        Product product3 = createProduct("Least Popular", 100);

        for (int i = 0; i < 10; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }
        for (int i = 0; i < 5; i++) {
            createReservation(product2, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }
        createReservation(product3, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Most Popular"))
                .andExpect(jsonPath("$[1].name").value("Medium Popular"))
                .andExpect(jsonPath("$[2].name").value("Least Popular"));
    }

    @Test
    void shouldHandleProductsWithSameReservationCount() throws Exception {
        Product product1 = createProduct("Product 1", 100);
        Product product2 = createProduct("Product 2", 100);

        for (int i = 0; i < 5; i++) {
            createReservation(product1, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
            createReservation(product2, ReservationStatus.CONFIRMED, Instant.now().minus(1, ChronoUnit.HOURS));
        }

        mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    private Product createProduct(String name, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setStock(stock);
        product.setVersion(0L);
        return productDao.save(product);
    }

    private void createReservation(Product product, ReservationStatus status, Instant createdAt) {
        Reservation reservation = new Reservation();
        reservation.setProduct(product);
        reservation.setQuantity(1);
        reservation.setStatus(status);
        reservation.setCreatedAt(createdAt);
        reservation.setExpiresAt(createdAt.plus(10, ChronoUnit.MINUTES));
        reservationDao.save(reservation);
    }
}