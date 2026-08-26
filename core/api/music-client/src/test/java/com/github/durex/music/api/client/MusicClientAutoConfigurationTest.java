package com.github.durex.music.api.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.durex.music.api.MusicApi;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    classes = MusicClientAutoConfigurationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MusicClientAutoConfigurationTest {

  private static final HttpServer SERVER = startServer();

  @Autowired private MusicApi musicApi;

  @DynamicPropertySource
  static void clientProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.http.serviceclient.music.base-url",
        () -> "http://127.0.0.1:" + SERVER.getAddress().getPort());
  }

  @AfterAll
  static void stopServer() {
    SERVER.stop(0);
  }

  @Test
  void autoConfigurationProvidesNamedMusicHttpClient() {
    var response = musicApi.get("music-1");
    assertEquals("music-1", response.result().getId());
    assertEquals("Client Runtime Song", response.result().getTitle());
    assertNull(response.error());
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}

  private static HttpServer startServer() {
    try {
      var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext(
          "/v1/music/music-1",
          exchange -> {
            var body =
                "{\"result\":{\"id\":\"music-1\",\"title\":\"Client Runtime Song\"},"
                    + "\"error\":null}";
            var bytes = body.getBytes(UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
              output.write(bytes);
            }
          });
      server.start();
      return server;
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
}
