package com.github.durex.music.spring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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

  @Test
  void listByTitleKeepsLegacyHttpContract() throws Exception {
    var musicService = mock(MusicService.class);
    var music = new Music();
    music.setId("music-1");
    music.setTitle("Spring Song");
    when(musicService.getMusicsByTitle("Spring Song")).thenReturn(List.of(music));

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MusicHttpController(musicService)).build();

    mvc.perform(get("/v1/music/").queryParam("title", "Spring Song"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result[0].id").value("music-1"))
        .andExpect(jsonPath("$.error.message").value("OK"));

    verify(musicService).getMusicsByTitle("Spring Song");
  }

  @Test
  void createDelegatesToMusicService() throws Exception {
    var musicService = mock(MusicService.class);
    when(musicService.createMusic(any(Music.class))).thenReturn(1);

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MusicHttpController(musicService)).build();

    mvc.perform(
            post("/v1/music/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"music-1\",\"title\":\"Spring Song\",\"playId\":\"play-1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value(1))
        .andExpect(jsonPath("$.error.message").value("OK"));

    verify(musicService).createMusic(any(Music.class));
  }

  @Test
  void updateDelegatesToMusicService() throws Exception {
    var musicService = mock(MusicService.class);
    when(musicService.updateMusic(any(Music.class))).thenReturn(1);

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MusicHttpController(musicService)).build();

    mvc.perform(
            put("/v1/music/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"music-1\",\"title\":\"Updated Song\",\"playId\":\"play-1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value(1));

    verify(musicService).updateMusic(any(Music.class));
  }

  @Test
  void deleteByIdDelegatesToMusicService() throws Exception {
    var musicService = mock(MusicService.class);
    when(musicService.deleteMusicById("music-1")).thenReturn(1);

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MusicHttpController(musicService)).build();

    mvc.perform(delete("/v1/music/music-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value(1));

    verify(musicService).deleteMusicById("music-1");
  }
}
