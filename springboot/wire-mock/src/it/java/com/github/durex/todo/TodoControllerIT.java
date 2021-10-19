package com.github.durex.todo;

import static com.github.durex.utils.MockConstants.API_TODOS;
import static com.github.durex.utils.MockConstants.CONTENT_TYPE;
import static com.github.durex.utils.MockConstants.TODOS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.durex.utils.BaseWireMock;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class TodoControllerIT extends BaseWireMock {
  @Autowired private WebTestClient webTestClient;

  @Test
  void testGetAllTodosShouldReturnDataFromClient() {
    wireMockServer.stubFor(
        get(TODOS)
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(
                        "[{\"userId\": 1,\"id\": 1,\"title\": \"Learn Spring Boot 3.0\", \"completed\": false},"
                            + "{\"userId\": 1,\"id\": 2,\"title\": \"Learn WireMock\", \"completed\": true}]")));

    webTestClient
        .get()
        .uri(API_TODOS)
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
    wireMockServer.stubFor(
        get(TODOS).willReturn(aResponse().withStatus(403).withFixedDelay(2000)) // milliseconds
        );

    webTestClient
        .get()
        .uri(API_TODOS)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
  }

  @Test
  void basicWireMockExample() {
    wireMockServer.stubFor(
        get(urlEqualTo(TODOS))
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("todo-api/response-200.json")));

    webTestClient
        .get()
        .uri(API_TODOS)
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
