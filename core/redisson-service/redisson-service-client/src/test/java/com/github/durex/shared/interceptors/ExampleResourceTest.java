package com.github.durex.shared.interceptors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ExampleResourceTest {

  @Test
  void testHelloEndpoint() {
    given()
        .when()
        .get("/hello")
        .then()
        .statusCode(200)
        // expect the value of the interceptor
        .body(is("Hello"));
  }
}
