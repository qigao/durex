package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.api.MusicApi;
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
    assertEquals("OK", response.getError().getMessage());
  }
}
