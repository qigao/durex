package com.github.durex.music.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class CredentialsConfigTest {
  @InjectMock CredentialsConfig config;

  @Test
  void testGetToken() {
    Mockito.when(config.getToken()).thenReturn("token");
    assertEquals("token", config.getToken());
  }

  @Test
  void testGetAppId() {
    Mockito.when(config.getAppKey()).thenReturn("appId");
    assertEquals("appId", config.getAppKey());
  }
}
