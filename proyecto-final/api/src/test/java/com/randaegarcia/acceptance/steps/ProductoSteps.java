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

import java.util.Map;

public class ProductoSteps {
    private Response response;
    private String accessToken;

    @Before
    public void setup() {
        // Fetch JWT token before each test
        accessToken = getKeycloakToken();
    }

    @When("envío una solicitud POST a {string} con los datos del producto:")
    public void envioSolicitudPost(String endpoint, Map<String, String> productData) {
        Producto producto = new Producto();
        producto.name = productData.get("name");
        producto.description = productData.get("description");
        producto.category = ProductCategory.valueOf(productData.get("category"));
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
    }

    @Then("recibo un código de estado {int}")
    public void reciboUnCodigoDeEstado(int statusCode) {
        Assertions.assertEquals(statusCode, response.getStatusCode());
    }

    @Then("el producto creado tiene el nombre {string}")
    public void elProductoCreadoTieneElNombre(String expectedName) {
        String actualName = response.jsonPath().getString("name");
        Assertions.assertEquals(expectedName, actualName);
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
