package com.randaegarcia.acceptance.steps;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.domain.model.ProductCategory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductoSteps {
    private Response response;
    private String accessToken;
    private Long createdProductId;

    @Before
    public void setup() {
        // Fetch JWT token before each test
        try {
            accessToken = getKeycloakToken();
            System.out.println("Obtained JWT token: " + accessToken);
        } catch (Exception e) {
            System.err.println("Failed to obtain Keycloak token: " + e.getMessage());
            throw new RuntimeException("Failed to initialize test due to Keycloak token retrieval failure", e);
        }
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Given("que existen productos en el sistema")
    public void queExistenProductosEnElSistema() {
        // Create a test product to ensure there are products for listing
        Producto producto = new Producto();
        producto.name = "Laptop Test " + UUID.randomUUID();
        producto.description = "Laptop de prueba para listado";
        producto.category = ProductCategory.Electronics;
        producto.price = 1200.0;
        producto.cost = 900.0;
        producto.profit = 300.0;
        producto.quantity = 15L;
        producto.minimumStock = 5L;
        producto.isActive = true;

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(producto)
                .post("/productos");
        Assertions.assertEquals(200, response.getStatusCode(), "Failed to create test product: " + response.getBody().asString());
    }

    @Given("que existe un producto con nombre {string}")
    public void queExisteUnProductoConNombre(String name) {
        Producto producto = new Producto();
        String uniqueName = name.replace("${random.uuid}", UUID.randomUUID().toString());
        producto.name = uniqueName;
        producto.description = "Producto de prueba";
        producto.category = ProductCategory.Electronics;
        producto.price = 999.99;
        producto.cost = 800.0;
        producto.profit = 199.99;
        producto.quantity = 10L;
        producto.minimumStock = 5L;
        producto.isActive = true;

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(producto)
                .post("/productos");
        Assertions.assertEquals(200, response.getStatusCode(), "Failed to create test product: " + response.getBody().asString());
        createdProductId = response.jsonPath().getLong("id");
    }

    @Given("que existe un producto con nombre {string} y movimientos de inventario")
    public void queExisteUnProductoConMovimientos(String name) {
        // Create product
        queExisteUnProductoConNombre(name);
        // Add a stock movement by updating quantity
        response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("quantity", 5)
                .put("/productos/" + createdProductId + "/update-quantity");
        Assertions.assertEquals(200, response.getStatusCode(), "Failed to add stock movement: " + response.getBody().asString());
    }

    @When("envío una solicitud POST a {string} con los datos del producto:")
    public void envioSolicitudPost(String endpoint, Map<String, String> productData) {
        Producto producto = new Producto();
        String rawName = productData.get("name");
        producto.name = rawName.replace("${random.uuid}", UUID.randomUUID().toString());
        producto.description = productData.get("description");
        try {
            producto.category = ProductCategory.valueOf(productData.get("category"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid ProductCategory: " + productData.get("category") + ". Valid values: " + Arrays.toString(ProductCategory.values()), e);
        }
        producto.price = Double.parseDouble(productData.get("price"));
        producto.cost = Double.parseDouble(productData.get("cost"));
        producto.profit = Double.parseDouble(productData.get("profit"));
        producto.quantity = Long.parseLong(productData.get("quantity"));
        producto.minimumStock = Long.parseLong(productData.get("minimumStock"));
        producto.isActive = true;

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(producto)
                .post(endpoint);
        createdProductId = response.getStatusCode() == 200 ? response.jsonPath().getLong("id") : null;
    }

    @When("envío una solicitud GET a {string} con los parámetros:")
    public void envioSolicitudGetConParametros(String endpoint, Map<String, String> params) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("page", Integer.parseInt(params.get("page")))
                .queryParam("size", Integer.parseInt(params.get("size")))
                .queryParam("name", params.get("name"))
                .queryParam("category", params.get("category"))
                .queryParam("minPrice", Double.parseDouble(params.get("minPrice")))
                .queryParam("maxPrice", Double.parseDouble(params.get("maxPrice")))
                .get(endpoint);
    }

    @When("envío una solicitud GET a {string}")
    public void envioSolicitudGet(String endpoint) {
        String resolvedEndpoint = endpoint.replace("{id}", createdProductId.toString());
        response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .get(resolvedEndpoint);
    }

    @When("envío una solicitud PUT a {string} con los datos actualizados:")
    public void envioSolicitudPut(String endpoint, Map<String, String> productData) {
        Producto producto = new Producto();
        String rawName = productData.get("name");
        producto.name = rawName.replace("${random.uuid}", UUID.randomUUID().toString());
        producto.description = productData.get("description");
        try {
            producto.category = ProductCategory.valueOf(productData.get("category"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid ProductCategory: " + productData.get("category") + ". Valid values: " + Arrays.toString(ProductCategory.values()), e);
        }
        producto.price = Double.parseDouble(productData.get("price"));
        producto.cost = Double.parseDouble(productData.get("cost"));
        producto.profit = Double.parseDouble(productData.get("profit"));
        producto.quantity = Long.parseLong(productData.get("quantity"));
        producto.minimumStock = Long.parseLong(productData.get("minimumStock"));
        producto.isActive = true;
        producto.id = createdProductId;

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(producto)
                .put(endpoint);
    }

    @When("envío una solicitud PUT a {string} con cantidad {int}")
    public void envioSolicitudPutCantidad(String endpoint, int quantity) {
        String resolvedEndpoint = endpoint.replace("{id}", createdProductId.toString());
        response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("quantity", quantity)
                .put(resolvedEndpoint);
    }

    @When("envío una solicitud de historial a {string} con los parámetros:")
    public void envioSolicitudGetHistoria(String endpoint, Map<String, String> params) {
        String resolvedEndpoint = endpoint.replace("{id}", createdProductId.toString());
        response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("page", Integer.parseInt(params.get("page")))
                .queryParam("size", Integer.parseInt(params.get("size")))
                .get(resolvedEndpoint);
    }

    @Then("recibo un código de estado {int}")
    public void reciboUnCodigoDeEstado(int statusCode) {
        Assertions.assertEquals(statusCode, response.getStatusCode(), "Response body: " + response.getBody().asString());
    }

    @Then("el producto creado tiene el nombre {string}")
    public void elProductoCreadoTieneElNombre(String expectedName) {
        String actualName = response.jsonPath().getString("name");
        Assertions.assertTrue(actualName.startsWith(expectedName), "Expected name to start with '" + expectedName + "', but was: " + actualName);
    }

    @Then("la respuesta contiene una lista paginada de productos")
    public void laRespuestaContieneListaPaginadaProductos() {
        Assertions.assertNotNull(response.jsonPath().getList("content"), "Content list should not be null");
        Assertions.assertTrue(response.jsonPath().getInt("page") >= 0, "Page should be non-negative");
        Assertions.assertTrue(response.jsonPath().getInt("size") > 0, "Size should be positive");
        Assertions.assertTrue(response.jsonPath().getLong("totalElements") >= 0, "Total elements should be non-negative");
    }

    @Then("los productos en la lista tienen el nombre que comienza con {string}")
    public void losProductosTienenNombreQueComienzaCon(String prefix) {
        List<String> names = response.jsonPath().getList("content.name");
        for (String name : names) {
            Assertions.assertTrue(name.startsWith(prefix), "Product name '" + name + "' should start with '" + prefix + "'");
        }
    }

    @Then("el producto retornado tiene el nombre {string}")
    public void elProductoRetornadoTieneElNombre(String expectedName) {
        String actualName = response.jsonPath().getString("name");
        Assertions.assertTrue(actualName.startsWith(expectedName), "Expected name to start with '" + expectedName + "', but was: " + actualName);
    }

    @Then("el producto actualizado tiene el nombre {string}")
    public void elProductoActualizadoTieneElNombre(String expectedName) {
        String actualName = response.jsonPath().getString("name");
        Assertions.assertTrue(actualName.startsWith(expectedName), "Expected name to start with '" + expectedName + "', but was: " + actualName);
    }

    @Then("el producto tiene una cantidad de {int}")
    public void elProductoTieneCantidad(int expectedQuantity) {
        Long actualQuantity = response.jsonPath().getLong("quantity");
        Assertions.assertEquals(expectedQuantity, actualQuantity, "Expected quantity " + expectedQuantity + ", but was: " + actualQuantity);
    }

    @Then("la respuesta contiene una lista paginada de movimientos de inventario")
    public void laRespuestaContieneListaPaginadaMovimientos() {
        Assertions.assertNotNull(response.jsonPath().getList("content"), "Content list should not be null");
        Assertions.assertTrue(response.jsonPath().getInt("page") >= 0, "Page should be non-negative");
        Assertions.assertTrue(response.jsonPath().getInt("size") > 0, "Size should be positive");
        Assertions.assertTrue(response.jsonPath().getLong("totalElements") >= 0, "Total elements should be non-negative");
    }

    private String getKeycloakToken() {
        String keycloakUrl = "http://localhost:7080/realms/project/protocol/openid-connect/token";
        String clientId = "quarkus";
        String clientSecret = "U3IY4BEL28YXx35OstTgDNNdQDVpO3oo";
        String username = "admin";
        String password = "123456";

        Response tokenResponse = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret)
                .formParam("username", username)
                .formParam("password", password)
                .post(keycloakUrl);

        if (tokenResponse.getStatusCode() != 200) {
            throw new RuntimeException("Failed to obtain Keycloak token: " + tokenResponse.getStatusCode() + " - " + tokenResponse.getBody().asString());
        }

        return tokenResponse.jsonPath().getString("access_token");
    }
}
