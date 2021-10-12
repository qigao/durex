package com.github.durex.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseWireMock {
  protected static WireMockServer wireMockServer;

  @DynamicPropertySource
  static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("todo_base_url", wireMockServer::baseUrl);
  }

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    wireMockServer.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMockServer.stop();
  }

  @BeforeEach
  void clearWireMock() {
    System.out.println("Stored stubbings: " + wireMockServer.getStubMappings().size());
    wireMockServer.resetAll();
    System.out.println("Stored stubbings after reset: " + wireMockServer.getStubMappings().size());
  }
}
