package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.api.MusicApi;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootTest(
    classes = MusicSpringApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MusicHttpContractIntegrationTest {

  @LocalServerPort int port;

  @Test
  void reusableContractCallsRealSpringServer() {
    var restClient = RestClient.builder().baseUrl("http://127.0.0.1:" + port).build();
    var factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    MusicApi client = factory.createClient(MusicApi.class);

    var response = client.get("music-1");

    assertEquals("music-1", response.getResult().getId());
    assertEquals("Spring Runtime Song", response.getResult().getTitle());
    assertNull(response.getError());
  }

  @Test
  void missingMusicUsesCanonicalNotFoundErrorResponse() throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/music/missing"))
            .GET()
            .build();

    var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(404, response.statusCode());
    assertTrue(response.body().contains("\"result\":null"));
    assertTrue(response.body().contains("\"message\":\"music not found\""));
    assertTrue(response.body().contains("\"errorCode\":\"ENTITY_NOT_FOUND\""));
    assertFalse(response.body().contains("\"caller\""));
  }
}
