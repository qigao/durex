package com.github.durex.json;

import static com.github.durex.pojo.MockUtils.getMockURL;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.json.model.User;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WireMockJunit5Test {
  WireMockServer wireMockServer;
  public static final String USER_LIST_JSON = "/json/users.json";
  public static final String USER_JSON = "/json/user.json";
  private final String route = "/junit5/json";

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(8090);
    wireMockServer.start();
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void testOnlineFileToObjectList() throws IOException {
    wireMockServer.stubFor(
        get(urlEqualTo(route))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withStatus(200)
                    .withBodyFile(USER_LIST_JSON)));
    var user = SerDe.toObject(new TypeReference<List<User>>() {}, getMockURL(USER_LIST_JSON));
    assertEquals(200, user.size());
  }

  @Test
  void testOnlineFileToObject() throws IOException {
    wireMockServer.stubFor(
        get(urlEqualTo(route))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withStatus(200)
                    .withBodyFile(USER_JSON)));
    var user = SerDe.toObject(User.class, getMockURL(USER_JSON));
    assertEquals(1, user.getId());
  }
}
