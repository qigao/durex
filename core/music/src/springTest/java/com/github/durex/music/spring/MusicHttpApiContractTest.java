package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.api.MusicApi;
import org.junit.jupiter.api.Test;

class MusicHttpApiContractTest {

  @Test
  void controllerImplementsReusableMusicApi() {
    assertTrue(MusicApi.class.isAssignableFrom(MusicHttpController.class));
  }
}
