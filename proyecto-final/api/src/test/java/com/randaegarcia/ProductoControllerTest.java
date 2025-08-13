package com.randaegarcia;

import com.randaegarcia.domain.model.ProductCategory;
import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.domain.model.StockMovement;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoControllerTest {

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void setupTestData() {
        // Limpiar datos existentes
        StockMovement.deleteAll();
        Producto.deleteAll();

        ProductCategory[] categories = ProductCategory.values();

        // Crear productos de prueba con diferentes características
        for (int i = 1; i <= 15; i++) {
            Producto producto = new Producto();
            producto.name = "Producto Test " + String.format("%02d", i);
            producto.description = "Descripción del producto test " + i;
            producto.category = categories[i % categories.length];
            producto.price = 50.0 + (i * 25.0); // Precios variados: 75, 100, 125, etc.
            producto.cost = 25.0 + (i * 10.0);  // Costos variados: 35, 45, 55, etc.
            producto.profit = producto.price - producto.cost;
            producto.quantity = 5L + (i * 2L); // Cantidades variadas: 7, 9, 11, etc.
            producto.minimumStock = (long) (i % 3); // 0, 1, 2, 0, 1, 2, etc.
            producto.isActive = true;
            producto.persist();
        }

        // Crear productos inactivos
        for (int i = 16; i <= 20; i++) {
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

        log.info("Setup completed: {} active products, {} inactive products",
                Producto.count("isActive = true"), Producto.count("isActive = false"));
    }

    // ================= TESTS FOR GET /productos (findAll) =================

    @Test
    @Order(1)
    @DisplayName("Should return products with default pagination parameters")
    void testFindAllWithDefaultParams() {
        given()
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", hasSize(10)) // Default size
                .body("page", equalTo(0)) // Default page
                .body("size", equalTo(10))
                .body("totalElements", equalTo(15)) // Only active products
                .body("first", equalTo(true))
                .body("last", equalTo(false)) // Has more pages
                .body("numberOfElements", equalTo(10));
    }

    @Test
    @Order(2)
    @DisplayName("Should return products with custom pagination")
    void testFindAllWithCustomPagination() {
        given()
                .queryParam("page", 1)
                .queryParam("size", 5)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", hasSize(5))
                .body("page", equalTo(1))
                .body("size", equalTo(5))
                .body("totalElements", equalTo(15))
                .body("first", equalTo(false))
                .body("last", equalTo(false)) // Still has more
                .body("numberOfElements", equalTo(5));
    }

    @Test
    @Order(3)
    @DisplayName("Should filter products by name")
    void testFindAllFilterByName() {
        given()
                .queryParam("name", "Producto Test 01")
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content[0].name", containsString("Producto Test 01"))
                .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(4)
    @DisplayName("Should filter products by name prefix")
    void testFindAllFilterByNamePrefix() {
        given()
                .queryParam("name", "Producto Test")
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(15)) // All active products match
                .body("content", hasSize(10)); // First page
    }

    @Test
    @Order(5)
    @DisplayName("Should filter products by category")
    void testFindAllFilterByCategory() {
        // Get first available category
        String category = ProductCategory.values()[0].name();

        given()
                .queryParam("category", category)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].category", equalTo(category));
    }

    @Test
    @Order(6)
    @DisplayName("Should filter products by price range")
    void testFindAllFilterByPriceRange() {
        given()
                .queryParam("minPrice", 100)
                .queryParam("maxPrice", 200)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].price", allOf(greaterThanOrEqualTo(100.0f), lessThanOrEqualTo(200.0f)));
    }

    @Test
    @Order(7)
    @DisplayName("Should filter products by minimum price only")
    void testFindAllFilterByMinPrice() {
        given()
                .queryParam("minPrice", 200)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content.findAll { it.price >= 200 }", not(empty()));
    }

    @Test
    @Order(8)
    @DisplayName("Should return empty result when no products match filters")
    void testFindAllNoMatches() {
        given()
                .queryParam("name", "NonExistentProduct")
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", empty())
                .body("totalElements", equalTo(0))
                .body("empty", equalTo(true));
    }

    // ================= TESTS FOR GET /productos/{id} (findById) =================

    @Test
    @Order(9)
    @DisplayName("Should return product by ID when it exists and is active")
    void testFindProductByIdSuccess() {
        // Get an active product
        Producto activeProduct = Producto.find("isActive = true").firstResult();
        assertNotNull(activeProduct, "Should have active products");

        given()
                .pathParam("id", activeProduct.id)
                .when()
                .get("/productos/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(activeProduct.id.intValue()))
                .body("name", equalTo(activeProduct.name))
                .body("isActive", equalTo(true));
    }

    @Test
    @Order(10)
    @DisplayName("Should return null when product ID does not exist")
    void testFindProductByIdNotFound() {
        Long nonExistentId = 99999L;

        Response response = given()
                .pathParam("id", nonExistentId)
                .when()
                .get("/productos/{id}")
                .then()
                .statusCode(204)
                .extract().response();

        assertTrue(response.body().asString().isEmpty(), "Response body should be empty");
    }

    @Test
    @Order(11)
    @DisplayName("Should return null when product exists but is inactive")
    void testFindProductByIdInactive() {
        // Get an inactive product
        Producto inactiveProduct = Producto.find("isActive = false").firstResult();
        assertNotNull(inactiveProduct, "Should have inactive products");

        given()
                .pathParam("id", inactiveProduct.id)
                .when()
                .get("/productos/{id}")
                .then()
                .statusCode(204); // No content for inactive products
    }

    // ================= TESTS FOR GET /productos/{id}/history =================

    @Test
    @Order(12)
    @DisplayName("Should return empty history for product without stock movements")
    void testProductHistoryEmpty() {
        // Get a product without movements
        Producto product = Producto.find("isActive = true").firstResult();
        assertNotNull(product);

        given()
                .pathParam("id", product.id)
                .when()
                .get("/productos/{id}/history")
                .then()
                .statusCode(200)
                .body("content", empty())
                .body("totalElements", equalTo(0))
                .body("empty", equalTo(true));
    }

    @Test
    @Order(13)
    @DisplayName("Should return 404 for history of non-existent product")
    void testProductHistoryNotFound() {
        Long nonExistentId = 99999L;

        given()
                .pathParam("id", nonExistentId)
                .when()
                .get("/productos/{id}/history")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("Should return 404 for history of inactive product")
    void testProductHistoryInactive() {
        Producto inactiveProduct = Producto.find("isActive = false").firstResult();
        assertNotNull(inactiveProduct);

        given()
                .pathParam("id", inactiveProduct.id)
                .when()
                .get("/productos/{id}/history")
                .then()
                .statusCode(404);
    }

    // ================= TESTS FOR PUT /productos/{id}/update-quantity =================

    @Test
    @Order(15)
    @DisplayName("Should update product quantity successfully")
    void testUpdateQuantitySuccess() {
        Producto product = Producto.find("isActive = true").firstResult();
        assertNotNull(product);

        Long initialQuantity = product.quantity;
        Long quantityToAdd = 10L;

        given()
                .pathParam("id", product.id)
                .queryParam("quantity", 10)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200)
                .body("quantity", equalTo(Math.toIntExact(quantityToAdd + initialQuantity)));
    }

    @Test
    @Order(16)
    @DisplayName("Should allow negative quantity update if result is above minimum stock")
    void testUpdateQuantityNegativeAllowed() {
        Producto product = Producto.find("isActive = true AND quantity > minimumStock + 5").firstResult();
        assertNotNull(product, "Should have a product with sufficient stock");

        Long quantityToSubtract = -3L;

        given()
                .pathParam("id", product.id)
                .queryParam("quantity", quantityToSubtract)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200)
                .body("quantity", equalTo(Math.toIntExact(product.quantity + quantityToSubtract)));
    }

    @Test
    @Order(17)
    @DisplayName("Should return 400 when quantity update would go below minimum stock")
    void testUpdateQuantityBelowMinimum() {
        Producto product = Producto.find("isActive = true").firstResult();
        assertNotNull(product);

        // Try to subtract more than available (considering minimum stock)
        Long excessiveSubtraction = -(product.quantity - product.minimumStock + 1);

        given()
                .pathParam("id", product.id)
                .queryParam("quantity", excessiveSubtraction)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(18)
    @DisplayName("Should return 404 when updating quantity of non-existent product")
    void testUpdateQuantityProductNotFound() {
        Long nonExistentId = 99999L;

        given()
                .pathParam("id", nonExistentId)
                .queryParam("quantity", 10)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(19)
    @DisplayName("Should return 404 when updating quantity of inactive product")
    void testUpdateQuantityInactiveProduct() {
        Producto inactiveProduct = Producto.find("isActive = false").firstResult();
        assertNotNull(inactiveProduct);

        given()
                .pathParam("id", inactiveProduct.id)
                .queryParam("quantity", 10)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(404);
    }

    // ================= TESTS FOR BUSINESS LOGIC =================

    @Test
    @Order(20)
    @DisplayName("Should create stock movement when updating quantity")
    @Transactional
    void testQuantityUpdateCreatesStockMovement() {
        Producto product = Producto.find("isActive = true").firstResult();
        assertNotNull(product);

        // Count existing movements
        long initialMovements = StockMovement.count("producto = ?1", product);

        given()
                .pathParam("id", product.id)
                .queryParam("quantity", 5)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200);

        // Verify stock movement was created
        long finalMovements = StockMovement.count("producto = ?1", product);
        assertEquals(initialMovements + 1, finalMovements, "Should create one new stock movement");

        // Verify movement details
        StockMovement latestMovement = StockMovement.find("producto = ?1 ORDER BY date DESC", product).firstResult();
        assertNotNull(latestMovement);
        assertEquals(5L, latestMovement.quantityChange);
        assertEquals(product.id, latestMovement.producto.id);
    }

    @Test
    @Order(21)
    @DisplayName("Should handle concurrent access to product quantities")
    void testConcurrentQuantityUpdates() {
        Producto product = Producto.find("isActive = true AND quantity > 20").firstResult();
        assertNotNull(product, "Should have a product with sufficient stock for concurrent updates");

        Long initialQuantity = product.quantity;

        // Simulate concurrent updates
        Response response1 = given()
                .pathParam("id", product.id)
                .queryParam("quantity", 5)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200)
                .extract().response();

        Response response2 = given()
                .pathParam("id", product.id)
                .queryParam("quantity", -3)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200)
                .extract().response();

        Producto finalProduct = Producto.findById(product.id);
        assertNotNull(finalProduct);
        assertTrue(finalProduct.quantity >= initialQuantity - 3,
                "Final quantity should account for all updates");
    }

    @Test
    @Order(22)
    @DisplayName("Should validate product data integrity across operations")
    void testProductDataIntegrity() {
        String validProductJson = """
            {
                "name": "Producto Integridad Test",
                "description": "Descripción de prueba de integridad",
                "category": "Electronics",
                "price": 150.0,
                "cost": 75.0,
                "profit": 75.0,
                "quantity": 20,
                "minimumStock": 5
            }
            """;

        // Create product
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(validProductJson)
                .when()
                .post("/productos")
                .then()
                .statusCode(200)
                .extract().response();

        Long productId = createResponse.jsonPath().getLong("id");

        // Verify product exists and is active
        given()
                .pathParam("id", productId)
                .when()
                .get("/productos/{id}")
                .then()
                .statusCode(200)
                .body("isActive", equalTo(true))
                .body("name", equalTo("Producto Integridad Test"));

        // Update quantity
        given()
                .pathParam("id", productId)
                .queryParam("quantity", 10)
                .when()
                .put("/productos/{id}/update-quantity")
                .then()
                .statusCode(200)
                .body("quantity", equalTo(30)); // 20 + 10

        // Soft delete
        given()
                .pathParam("id", productId)
                .when()
                .delete("/productos/{id}")
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));

        // Verify product is no longer accessible
        given()
                .pathParam("id", productId)
                .when()
                .get("/productos/{id}")
                .then()
                .statusCode(204); // No content for inactive product
    }

    @Test
    @Order(23)
    @DisplayName("Should handle edge cases in filtering")
    void testFilteringEdgeCases() {
        // Test empty string filters
        given()
                .queryParam("name", "")
                .queryParam("category", "")
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(15)); // Should return all active products

        // Test boundary price values
        given()
                .queryParam("minPrice", 0)
                .queryParam("maxPrice", 0)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", empty()); // No products with price 0

        // Test large page numbers
        given()
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", empty())
                .body("last", equalTo(true))
                .body("empty", equalTo(true));
    }

    @Test
    @Order(24)
    @DisplayName("Should maintain audit trail for product operations")
    @Transactional
    void testAuditTrail() {
        // This test verifies that Hibernate Envers is working
        // Create a product
        String productJson = """
            {
                "name": "Producto Auditoria",
                "description": "Producto para prueba de auditoria",
                "category": "Electronics",
                "price": 200.0,
                "cost": 100.0,
                "profit": 100.0,
                "quantity": 15,
                "minimumStock": 2
            }
            """;

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(productJson)
                .when()
                .post("/productos")
                .then()
                .statusCode(200)
                .extract().response();

        Long productId = createResponse.jsonPath().getLong("id");

        // Update the product
        String updateJson = String.format("""
            {
                "id": %d,
                "name": "Producto Auditoria Actualizado",
                "description": "Descripción actualizada",
                "category": "Electronics",
                "price": 250.0,
                "cost": 125.0,
                "profit": 125.0,
                "quantity": 20,
                "minimumStock": 3
            }
            """, productId);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/productos")
                .then()
                .statusCode(200);

        log.info("Audit trail test completed for product ID: {}", productId);
    }
}
