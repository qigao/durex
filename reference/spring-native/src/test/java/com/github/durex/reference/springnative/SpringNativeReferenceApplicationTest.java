package com.github.durex.reference.springnative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class SpringNativeReferenceApplicationTest {

  @Autowired private ApplicationContext applicationContext;

  @Autowired private HelloController helloController;

  @Test
  void springContextLoads() {
    assertNotNull(applicationContext);
  }

  @Test
  void helloControllerReturnsReferencePayload() {
    assertEquals("Hello from Spring Native", helloController.hello());
  }
}
