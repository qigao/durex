package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.durex.music.service.MusicService;
import com.github.durex.shared.exceptions.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class MusicSpringInterceptorTest {

  @Autowired private MusicService musicService;

  @Test
  void nullCheckerRunsThroughSpringAop() {
    var exception =
        assertThrows(ApiException.class, () -> musicService.getMusicsByTitle("missing-title"));

    assertEquals("No Data Returned", exception.getMessage());
  }

  @Test
  void valueCheckerRunsThroughSpringAop() {
    var exception =
        assertThrows(ApiException.class, () -> musicService.deleteMusicById("missing-id"));

    assertEquals("Unqualified Return Value", exception.getMessage());
  }
}
