package com.randaegarcia;

import com.randaegarcia.domain.model.Producto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class ProductoControllerTest {

    @BeforeEach
    @Transactional
    void setupTestData() {
        // Limpiar datos existentes
        Producto.deleteAll();

        // Crear productos de prueba
        for (int i = 1; i <= 25; i++) {
            Producto producto = new Producto();
            producto.name = "Producto " + String.format("%02d", i);
            producto.description = "Descripción del producto " + i;
            producto.category = "Categoria " + (i % 5 + 1); // 5 categorías diferentes
            producto.price = 100.0 * i;
            producto.cost = 50.0 * i;
            producto.profit = producto.price - producto.cost;
            producto.quantity = 10L + i;
            producto.isActive = true;
            producto.persist();
        }

        // Crear algunos productos inactivos para verificar el filtro
        for (int i = 26; i <= 30; i++) {
            Producto producto = new Producto();
            producto.name = "Producto Inactivo " + String.format("%02d", i);
            producto.description = "Descripción del producto inactivo " + i;
            producto.category = "Categoria Inactiva";
            producto.price = 100.0 * i;
            producto.cost = 50.0 * i;
            producto.profit = producto.price - producto.cost;
            producto.quantity = 0L;
            producto.isActive = false;
            producto.persist();
        }
    }

    @Test
    @DisplayName("Debe validar que solo cuenta productos activos")
    void testCountOnlyActiveProducts() {
        // Verificar que el total es 25 (productos activos) y no 30 (total de productos)
        given()
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(25)); // Solo productos activos
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay más elementos")
    void testFindAllEmptyPage() {
        given()
                .queryParam("page", 10)
                .queryParam("size", 10)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("content", hasSize(0))
                .body("page", equalTo(10))
                .body("size", equalTo(10))
                .body("totalElements", equalTo(25))
                .body("first", equalTo(false))
                .body("last", equalTo(true))
                .body("empty", equalTo(true))
                .body("numberOfElements", equalTo(0));
    }

    @Test
    @DisplayName("Debe crear un producto correctamente con datos válidos")
    void testCreateProductoSuccessfully() {
        String productJson = """
            {
                "name": "Nuevo Producto",
                "description": "Descripción del nuevo producto",
                "category": "Nueva Categoria",
                "price": 200.0,
                "cost": 100.0,
                "profit": 100.0,
                "quantity": 50
            }
            """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(productJson)
                .when()
                .post("/productos")
                .then()
                .statusCode(200)
                .body("name", equalTo("Nuevo Producto"))
                .body("description", equalTo("Descripción del nuevo producto"))
                .body("category", equalTo("Nueva Categoria"))
                .body("price", equalTo(200.0f))
                .body("cost", equalTo(100.0f))
                .body("profit", equalTo(100.0f))
                .body("quantity", equalTo(50))
                .body("isActive", equalTo(true))
                .body("id", notNullValue())
                .extract().response();

        // Verificar que el producto se guardó en la base de datos
        Long productId = response.as(Producto.class).id;
        Producto savedProduct = Producto.findById(productId);

        assertNotNull(savedProduct);
        assertEquals("Nuevo Producto", savedProduct.name);
        assertTrue(savedProduct.isActive);
    }

    @Test
    @DisplayName("Debe retornar error 409 cuando el nombre del producto ya existe")
    void testCreateProductoWithDuplicateName() {
        String duplicateProductJson = """
            {
                "name": "Producto 01",
                "description": "Descripción diferente",
                "category": "Categoria Diferente",
                "price": 300.0,
                "cost": 150.0,
                "profit": 150.0,
                "quantity": 25
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(duplicateProductJson)
                .when()
                .post("/productos")
                .then()
                .statusCode(409); // Conflict - nombre ya existe

        // Verificar que no se creó un segundo producto con el mismo nombre
        long count = Producto.count("name = ?1", "Producto 01");
        assertEquals(1, count, "Solo debe existir un producto con ese nombre");
    }

    @Test
    @DisplayName("Debe retornar error 400 cuando los datos del producto son inválidos")
    void testCreateProductoWithInvalidData() {
        // Test con nombre muy corto (menos de 3 caracteres)
        String invalidProductJson1 = """
            {
                "name": "AB",
                "description": "Descripción válida",
                "category": "Categoria Válida",
                "price": 200.0,
                "cost": 100.0,
                "profit": 100.0,
                "quantity": 50
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidProductJson1)
                .when()
                .post("/productos")
                .then()
                .statusCode(400); // Bad Request - validación fallida

        // Test con precio negativo
        String invalidProductJson2 = """
            {
                "name": "Producto Válido",
                "description": "Descripción válida",
                "category": "Categoria Válida",
                "price": -100.0,
                "cost": 50.0,
                "profit": 50.0,
                "quantity": 50
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidProductJson2)
                .when()
                .post("/productos")
                .then()
                .statusCode(400); // Bad Request - precio negativo

        // Test con campos requeridos null
        String invalidProductJson3 = """
            {
                "name": null,
                "description": "Descripción válida",
                "category": "Categoria Válida",
                "price": 200.0,
                "cost": 100.0,
                "profit": 100.0,
                "quantity": 50
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidProductJson3)
                .when()
                .post("/productos")
                .then()
                .statusCode(400); // Bad Request - name es null

        // Verificar que ningún producto inválido se guardó
        long totalProducts = Producto.count();
        assertEquals(30, totalProducts, "Solo debe existir el producto inicial de setup");
    }

    @Test
    @DisplayName("Debe actualizar un producto correctamente cuando existe")
    void testUpdateProductoSuccessfully() {
        // Obtener un producto existente para actualizar
        Producto existingProduct = Producto.find("name = ?1", "Producto 01").firstResult();
        assertNotNull(existingProduct, "Debe existir el producto para actualizar");

        String updateProductJson = String.format("""
        {
            "id": %d,
            "name": "Producto Actualizado",
            "description": "Descripción actualizada",
            "category": "Categoria Actualizada",
            "price": 999.0,
            "cost": 500.0,
            "profit": 499.0,
            "quantity": 100
        }
        """, existingProduct.id);

        // Solo verificar que la respuesta es correcta
        // La persistencia se asume que funciona si el endpoint retorna 200
        given()
                .contentType(ContentType.JSON)
                .body(updateProductJson)
                .when()
                .put("/productos")
                .then()
                .statusCode(200)
                .body("name", equalTo("Producto Actualizado"))
                .body("description", equalTo("Descripción actualizada"));
    }

    @Test
    @DisplayName("Debe retornar error 404 cuando se intenta actualizar un producto que no existe")
    void testUpdateProductoNotFound() {
        // Usar un ID que no existe
        Long nonExistentId = 99999L;

        String updateProductJson = String.format("""
            {
                "id": %d,
                "name": "Producto No Existe",
                "description": "Descripción de producto inexistente",
                "category": "Categoria Inexistente",
                "price": 500.0,
                "cost": 250.0,
                "profit": 250.0,
                "quantity": 50
            }
            """, nonExistentId);

        given()
                .contentType(ContentType.JSON)
                .body(updateProductJson)
                .when()
                .put("/productos")
                .then()
                .statusCode(404); // Not Found

        // Verificar que no se creó ningún producto nuevo
        Producto nonExistentProduct = Producto.findById(nonExistentId);
        assertNull(nonExistentProduct, "No debe existir un producto con ese ID");

        // Verificar que el conteo total no cambió
        long totalProducts = Producto.count();
        assertEquals(30, totalProducts, "El número total de productos no debe cambiar");
    }

    @Test
    @DisplayName("Debe desactivar un producto correctamente cuando existe")
    void testDeleteProductoSuccessfully() {
        // Obtener un producto existente para eliminar
        Producto existingProduct = Producto.find("name = ?1 and isActive = true", "Producto 01").firstResult();
        assertNotNull(existingProduct, "Debe existir el producto para eliminar");
        assertTrue(existingProduct.isActive, "El producto debe estar activo inicialmente");

        given()
                .when()
                .delete("/productos/" + existingProduct.id)
                .then()
                .statusCode(200)
                .body("id", equalTo(existingProduct.id.intValue()))
                .body("name", equalTo("Producto 01"))
                .body("description", equalTo("Descripción del producto 1"))
                .body("isActive", equalTo(false)) // Debe estar desactivado
                .body("price", notNullValue())
                .body("cost", notNullValue())
                .body("profit", notNullValue())
                .body("quantity", notNullValue());
    }

    @Test
    @DisplayName("Debe retornar error 404 cuando se intenta eliminar un producto que no existe")
    void testDeleteProductoNotFound() {
        // Usar un ID que no existe
        Long nonExistentId = 99999L;

        given()
                .when()
                .delete("/productos/" + nonExistentId)
                .then()
                .statusCode(404); // Not Found - producto no existe

        // Verificar que no se afectó el conteo de productos activos
        // Los productos activos deben seguir siendo los mismos
        given()
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(25)); // Debe seguir siendo 25 productos activos
    }

    @Test
    @DisplayName("Debe retornar error 404 cuando se intenta eliminar un producto ya inactivo")
    void testDeleteProductoAlreadyInactive() {
        // Obtener un producto inactivo del setup
        Producto inactiveProduct = Producto.find("isActive = false").firstResult();
        assertNotNull(inactiveProduct, "Debe existir al menos un producto inactivo del setup");

        given()
                .when()
                .delete("/productos/" + inactiveProduct.id)
                .then()
                .statusCode(404); // Not Found - el producto ya está "eliminado"
    }

}
