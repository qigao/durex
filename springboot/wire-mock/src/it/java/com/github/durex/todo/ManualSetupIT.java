package com.github.durex.todo;

import static com.github.durex.utils.MockConstants.API_TODOS;
import static com.github.durex.utils.MockConstants.CONTENT_TYPE;
import static com.github.durex.utils.MockConstants.TODOS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.utils.BaseWireMock;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManualSetupIT extends BaseWireMock {

  @Autowired private WebTestClient webTestClient;

  @Test
  @Order(1)
  void basicWireMockExample() {
    wireMockServer.stubFor(
        get(TODOS)
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("[]")));

    webTestClient
        .get()
        .uri(API_TODOS)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(0);
  }

  @Test
  @Order(2)
  void wireMockRequestMatching() {
    wireMockServer.stubFor(
        get(urlEqualTo("/users"))
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("[]")));

    webTestClient.get().uri(API_TODOS).exchange().expectStatus().is5xxServerError();
  }

  @Test
  void wireMockRequestMatchingPriority() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo(TODOS))
            .atPriority(1)
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("[]")));

    wireMockServer.stubFor(
        get(urlEqualTo(TODOS)).atPriority(10).willReturn(aResponse().withStatus(500)));

    webTestClient
        .get()
        .uri(API_TODOS)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(0);
  }

  @Test
  void wireMockRequestMatchingWithData() {
    wireMockServer.stubFor(
        get(urlEqualTo(TODOS))
            .willReturn(
                aResponse()
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("todo-api/response-200.json")
                    .withFixedDelay(1_000)));

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

    wireMockServer.verify(
        exactly(1),
        getRequestedFor(urlEqualTo(TODOS))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("X-Auth", equalTo("duke")));

    List<ServeEvent> events = wireMockServer.getAllServeEvents();

    events.get(0).getRequest().containsHeader("X-Auth");

    List<LoggedRequest> allUnmatchedRequests = wireMockServer.findAllUnmatchedRequests();

    assertEquals(0, allUnmatchedRequests.size());
  }
}
