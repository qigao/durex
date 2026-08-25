package com.github.durex.music.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MusicSpringApplication.class)
@AutoConfigureMockMvc
class MusicSpringApplicationTest {

  @Autowired private MockMvc mvc;

  @Test
  void getByIdRunsThroughSpringJooqAndH2() throws Exception {
    mvc.perform(get("/v1/music/music-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.id").value("music-1"))
        .andExpect(jsonPath("$.result.title").value("Spring Runtime Song"))
        .andExpect(jsonPath("$.error").doesNotExist());
  }
}
