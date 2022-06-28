package com.github.durex.redisson.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class QuarkusRedissonClientResourceTest {

  @Test
  void testRemoteService() {
    given()
        .when()
        .get("/quarkus-redisson-client/remoteService")
        .then()
        .statusCode(200)
        .body(is("executed"));
  }

  @Test
  void testMap() {
    given().when().get("/quarkus-redisson-client/map").then().statusCode(200).body(is("2"));
  }

  @Test
  void testPingAll() {
    given().when().get("/quarkus-redisson-client/pingAll").then().statusCode(200).body(is("OK"));
  }

  @Test
  void testExecuteTask() {
    given()
        .when()
        .get("/quarkus-redisson-client/executeTask")
        .then()
        .statusCode(200)
        .body(is("hello"));
  }
}
