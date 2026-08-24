package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.api.MusicApi;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

class MusicHttpApiContractTest {

  @Test
  void controllerImplementsReusableMusicApi() {
    assertTrue(MusicApi.class.isAssignableFrom(MusicHttpController.class));
  }

  @Test
  void listContractExposesOnlyRequiredTitleFilter() throws Exception {
    var method = MusicApi.class.getMethod("list", String.class);

    assertEquals(1, method.getParameterCount());
    var requestParam = method.getParameters()[0].getAnnotation(RequestParam.class);
    assertEquals("title", requestParam.value());
    assertTrue(requestParam.required());
  }
}
