package com.interview.support;

import com.interview.api.LocalApiServer;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public abstract class ApiTestBase {
    private static LocalApiServer server;

    @BeforeAll
    static void startApi() throws IOException {
        server = new LocalApiServer();
        server.start();
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = server.port();
    }

    @AfterAll
    static void stopApi() {
        if (server != null) server.close();
        RestAssured.reset();
    }

    protected RequestSpecification jsonRequest() {
        return RestAssured.given().contentType("application/json");
    }

    protected String loginToken() {
        return jsonRequest()
                .body("{\"username\":\"qa\",\"password\":\"secret\"}")
                .post("/api/auth/login")
                .then().statusCode(200).extract().path("token");
    }
}
