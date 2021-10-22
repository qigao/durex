package com.github.durex.demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DemoResourceTest {

  @Test
  void testHelloEndpoint() {
    given().when().get("/hello").then().statusCode(200).body(is("Hello RESTEasy"));
  }
}
