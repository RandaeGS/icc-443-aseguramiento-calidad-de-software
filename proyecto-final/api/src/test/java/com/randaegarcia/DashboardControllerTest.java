package com.randaegarcia;

import com.randaegarcia.domain.model.ProductCategory;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.domain.model.StockMovement;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void setupTestData() {
        log.info("=== SETUP DASHBOARD TEST DATA - START ===");

        StockMovement.deleteAll();
        Producto.deleteAll();

        Random random = new Random(42); // Fixed seed for consistent tests
        ProductCategory[] categories = ProductCategory.values();

        for (int i = 1; i <= 10; i++) {
            Producto producto = new Producto();
            producto.name = "Producto " + String.format("%02d", i);
            producto.description = "Descripción del producto " + i;
            producto.category = categories[i % categories.length];
            producto.price = 100.0 * i;
            producto.cost = 50.0 * i;
            producto.profit = producto.price - producto.cost;
            producto.quantity = 10L + i;
            producto.minimumStock = 1L;
            producto.isActive = true;
            producto.persist();

            log.info("Created active product: {} with category: {}", producto.name, producto.category);
        }

        // Create inactive products (should be excluded from dashboard stats)
        for (int i = 11; i <= 15; i++) {
            Producto producto = new Producto();
            producto.name = "Producto Inactivo " + String.format("%02d", i);
            producto.description = "Descripción del producto inactivo " + i;
            producto.category = categories[i % categories.length];
            producto.price = 100.0 * i;
            producto.cost = 50.0 * i;
            producto.profit = producto.price - producto.cost;
            producto.quantity = 0L;
            producto.minimumStock = 0L;
            producto.isActive = false;
            producto.persist();
        }

        // Create stock movements for active products with different days and patterns
        List<Producto> activeProducts = Producto.find("isActive = true").list();

        LocalDateTime baseDate = LocalDateTime.now().minusDays(6); // Start from a week ago

        for (int day = 0; day < 7; day++) {
            LocalDateTime movementDate = baseDate.plusDays(day);

            // Create different number of movements per day for testing
            int movementsForDay = (day % 3) + 1; // 1-3 movements per day

            for (int movement = 0; movement < movementsForDay; movement++) {
                Producto producto = activeProducts.get((day + movement) % activeProducts.size());

                StockMovement stockMovement = new StockMovement();
                stockMovement.producto = producto;
                stockMovement.date = movementDate.plusHours(movement);
                stockMovement.quantityChange = 5L + (movement * 2);
                stockMovement.actualQuantity = producto.quantity + stockMovement.quantityChange;
                stockMovement.username = "test-user";
                stockMovement.persist();

                log.info("Created stock movement for {} on {} ({})",
                        producto.name, movementDate.getDayOfWeek(), movementDate.toLocalDate());
            }
        }

        // Create movements for inactive products (should be excluded)
        List<Producto> inactiveProducts = Producto.find("isActive = false").list();
        if (!inactiveProducts.isEmpty()) {
            StockMovement inactiveMovement = new StockMovement();
            inactiveMovement.producto = inactiveProducts.get(0);
            inactiveMovement.date = LocalDateTime.now();
            inactiveMovement.quantityChange = 10L;
            inactiveMovement.actualQuantity = 10L;
            inactiveMovement.username = "test-user";
            inactiveMovement.persist();

            log.info("Created movement for inactive product (should be excluded from stats)");
        }

        log.info("=== SETUP DASHBOARD TEST DATA - END ===");
        log.info("Active products: {}", Producto.count("isActive = true"));
        log.info("Inactive products: {}", Producto.count("isActive = false"));
        log.info("Total stock movements: {}", StockMovement.count());
    }

    @Test
    @Order(1)
    @DisplayName("Should return movements per day for the week")
    void testMovementsPerDay() {
        var response = given()
                .when()
                .get("/dashboard/movements-per-day")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(7)) // 7 days in a week
                .extract().response();

        List<Long> movementsPerDay = response.jsonPath().getList("", Long.class);

        // Verify we have 7 days
        assertEquals(7, movementsPerDay.size(), "Should return exactly 7 days");

        // Verify all values are non-negative
        movementsPerDay.forEach(count -> {
            assertTrue(count >= 0, "Movement count should be non-negative: " + count);
        });

        // Verify total movements > 0 (we created movements in setup)
        long totalMovements = movementsPerDay.stream().mapToLong(Long::longValue).sum();
        assertTrue(totalMovements > 0, "Should have some movements recorded");

        log.info("Movements per day: {}", movementsPerDay);
    }

    @Test
    @Order(2)
    @DisplayName("Should return movements per category for all product categories")
    void testMovementsPerCategory() {
        var response = given()
                .when()
                .get("/dashboard/movements-per-category")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        Map<String, Long> movementsPerCategory = response.jsonPath().getMap("", String.class, Long.class);

        // Verify all categories are present
        ProductCategory[] categories = ProductCategory.values();
        assertEquals(categories.length, movementsPerCategory.size(),
                "Should return all product categories");

        for (ProductCategory category : categories) {
            assertTrue(movementsPerCategory.containsKey(category.name()),
                    "Should contain category: " + category.name());

            Long count = movementsPerCategory.get(category.name());
            assertTrue(count >= 0, "Movement count should be non-negative for " + category.name());
        }

        log.info("Movements per category: {}", movementsPerCategory);
    }

    @Test
    @Order(3)
    @DisplayName("Should return the most moved product")
    void testMostMovedProduct() {
        given()
                .when()
                .get("/dashboard/most-moved-product")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("containsKey('name')", is(true))
                .body("containsKey('quantity')", is(true))
                .body("name", notNullValue())
                .body("quantity", greaterThan(0));
    }

    @Test
    @Order(4)
    @DisplayName("Should return the least moved product")
    void testLeastMovedProduct() {
        given()
                .when()
                .get("/dashboard/least-moved-product")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("containsKey('name')", is(true))
                .body("containsKey('quantity')", is(true))
                .body("name", notNullValue())
                .body("quantity", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(5)
    @DisplayName("Should return the most demanded category")
    void testMostDemandedCategory() {
        var response = given()
                .when()
                .get("/dashboard/most-demanded-category")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("containsKey('description')", is(true))
                .body("containsKey('quantity')", is(true))
                .body("description", notNullValue())
                .body("quantity", greaterThan(0))
                .extract().response();

        String categoryDescription = response.jsonPath().getString("description");
        Long quantity = response.jsonPath().getLong("quantity");

        // Verify it's a valid category
        boolean isValidCategory = Arrays.stream(ProductCategory.values())
                .anyMatch(cat -> cat.name().equals(categoryDescription));
        assertTrue(isValidCategory, "Should return a valid product category");

        log.info("Most demanded category: {} with {} movements", categoryDescription, quantity);
    }

    @Test
    @Order(6)
    @DisplayName("Should return the least demanded category")
    void testLeastDemandedCategory() {
        var response = given()
                .when()
                .get("/dashboard/least-demanded-category")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("containsKey('description')", is(true))
                .body("containsKey('quantity')", is(true))
                .body("description", notNullValue())
                .body("quantity", greaterThanOrEqualTo(0))
                .extract().response();

        String categoryDescription = response.jsonPath().getString("description");
        Long quantity = response.jsonPath().getLong("quantity");

        // Verify it's a valid category
        boolean isValidCategory = Arrays.stream(ProductCategory.values())
                .anyMatch(cat -> cat.name().equals(categoryDescription));
        assertTrue(isValidCategory, "Should return a valid product category");

        log.info("Least demanded category: {} with {} movements", categoryDescription, quantity);
    }

    @Test
    @Order(7)
    @DisplayName("Should only include movements from active products")
    @Transactional
    void testOnlyActiveProductsIncluded() {
        // Verify setup: we should have both active and inactive products
        long activeProductCount = Producto.count("isActive = true");
        long inactiveProductCount = Producto.count("isActive = false");

        assertTrue(activeProductCount > 0, "Should have active products");
        assertTrue(inactiveProductCount > 0, "Should have inactive products for testing");

        // Get movements for verification
        var movementsResponse = given()
                .when()
                .get("/dashboard/movements-per-day")
                .then()
                .statusCode(200)
                .extract().response();

        List<Long> movementsPerDay = movementsResponse.jsonPath().getList("", Long.class);
        long totalMovements = movementsPerDay.stream().mapToLong(Long::longValue).sum();

        // Verify that we have movements (from active products only)
        assertTrue(totalMovements > 0, "Should have movements from active products");

        // Check that movements count matches only active product movements
        long activeMovementsInDb = StockMovement.count("producto.isActive = true");
        long inactiveMovementsInDb = StockMovement.count("producto.isActive = false");

        log.info("Active movements in DB: {}, Inactive movements in DB: {}",
                activeMovementsInDb, inactiveMovementsInDb);
        log.info("Total movements from dashboard: {}", totalMovements);

        // The dashboard should only count active product movements
        assertEquals(activeMovementsInDb, totalMovements,
                "Dashboard should only count movements from active products");
    }

    @Test
    @Order(9)
    @DisplayName("Should verify data consistency across all endpoints")
    void testDataConsistency() {
        // Get data from all endpoints
        var movementsPerDay = given().get("/dashboard/movements-per-day")
                .then().statusCode(200).extract().jsonPath().getList("", Long.class);

        var movementsPerCategory = given().get("/dashboard/movements-per-category")
                .then().statusCode(200).extract().jsonPath().getMap("", String.class, Long.class);

        // Calculate totals
        long totalFromDays = movementsPerDay.stream().mapToLong(Long::longValue).sum();
        long totalFromCategories = movementsPerCategory.values().stream().mapToLong(Long::longValue).sum();

        // Both should represent the same total movements
        assertEquals(totalFromDays, totalFromCategories,
                "Total movements should be consistent across endpoints");

        log.info("Data consistency verified - Total movements: {}", totalFromDays);
    }
}
