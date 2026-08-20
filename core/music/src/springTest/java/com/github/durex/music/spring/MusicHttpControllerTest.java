package com.github.durex.music.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MusicHttpControllerTest {

  @Test
  void getByIdDelegatesToMusicServiceAndKeepsResponseEnvelope() throws Exception {
    var musicService = mock(MusicService.class);
    var music = new Music();
    music.setId("music-1");
    music.setTitle("Spring Song");
    when(musicService.getMusicById("music-1")).thenReturn(music);

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MusicHttpController(musicService)).build();

    mvc.perform(get("/v1/music/music-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.id").value("music-1"))
        .andExpect(jsonPath("$.result.title").value("Spring Song"))
        .andExpect(jsonPath("$.error.message").value("OK"));

    verify(musicService).getMusicById("music-1");
  }
}
