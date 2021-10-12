package com.github.durex.todo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import com.github.durex.utils.BaseWireMock;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class TodoControllerTest extends BaseWireMock {
  @Autowired private WebTestClient webTestClient;

  @Test
  void testGetAllTodosShouldReturnDataFromClient() {
    BaseWireMock.wireMockServer.stubFor(
        WireMock.get("/todos")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(
                        "[{\"userId\": 1,\"id\": 1,\"title\": \"Learn Spring Boot 3.0\", \"completed\": false},"
                            + "{\"userId\": 1,\"id\": 2,\"title\": \"Learn WireMock\", \"completed\": true}]")));

    webTestClient
        .get()
        .uri("/api/todos")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .jsonPath("$[0].title")
        .isEqualTo("Learn Spring Boot 3.0")
        .jsonPath("$.length()")
        .isEqualTo(2);
  }

  @Test
  void testGetAllTodosShouldPropagateErrorMessageFromClient() {
    BaseWireMock.wireMockServer.stubFor(
        WireMock.get("/todos")
            .willReturn(aResponse().withStatus(403).withFixedDelay(2000)) // milliseconds
        );

    webTestClient
        .get()
        .uri("/api/todos")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
  }

  @Test
  void basicWireMockExample() {
    BaseWireMock.wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/todos"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("todo-api/response-200.json")));

    webTestClient
        .get()
        .uri("/api/todos")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(3)
        .jsonPath("$[0].title")
        .isEqualTo("delectus aut autem");
  }
}
