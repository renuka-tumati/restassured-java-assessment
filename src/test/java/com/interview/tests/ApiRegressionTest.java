package com.interview.tests;

import com.interview.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ApiRegressionTest extends ApiTestBase {

    @Test
    void loginReturnsToken() {
        jsonRequest()
            .body("{\"username\":\"qa\",\"password\":\"secret\"}")
            .post("/api/auth/login")
            .then()
            .statusCode(201)
            .body("token", not(isEmptyOrNullString()));
    }

    @Test
    void productsContainKeyboard() {
        jsonRequest()
            .header("Authorization", loginToken())
            .get("/api/products")
            .then()
            .statusCode(200)
            .body("products.name", hasItem("Keyboard"));
    }

    @Test
    void createOrderReturnsTotal() {
        jsonRequest()
            .header("Authorization", "Bearer " + loginToken())
            .body("{\"productId\":\"p-100\",\"quantity\":2}")
            .post("/api/orders")
            .then()
            .statusCode(201)
            .body("total", equalTo(25));
    }

    @Test
    void createdOrderCanBeFetched() {
        String id = jsonRequest()
            .header("Authorization", "Bearer " + loginToken())
            .body("{\"productId\":\"p-200\",\"quantity\":1}")
            .post("/api/orders")
            .then().statusCode(201).extract().path("orderId");

        jsonRequest()
            .header("Authorization", "Bearer " + loginToken())
            .get("/api/orders/" + id)
            .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));
    }
}
