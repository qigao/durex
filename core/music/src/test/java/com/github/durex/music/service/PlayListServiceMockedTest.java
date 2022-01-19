package com.github.durex.music.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.github.durex.music.entity.PlayList;
import com.github.durex.music.model.PlayListRequest;
import com.github.durex.music.repository.PlayListRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayListServiceMockedTest {
  @Inject PlaylistService playListService;
  @InjectMock PlayListRepository playListRepository;

  @Test
  void testDeletePlayListByIDReturnTrue() {
    when(playListRepository.deletePlayListById(any())).thenReturn(1);
    assertEquals(1, playListService.deletePlaylist("1"));
  }

  @Test
  void testDeletePlayListByIDReturnFalse() {
    when(playListRepository.deletePlayListById(any())).thenReturn(0);
    assertNotEquals(1, playListService.deletePlaylist("1"));
  }

  @Test
  void testDeletePlaylistByIdAndEditorReturnTrue() {
    when(playListRepository.deletePlayListByIdAndEditor(any(), any())).thenReturn(1);
    assertEquals(1, playListService.deletePlayListByIdAndEditor("1", "1"));
  }

  @Test
  void testDeletePlaylistByIdAndEditorReturnFalse() {
    when(playListRepository.deletePlayListByIdAndEditor(any(), any())).thenReturn(0);
    assertNotEquals(1, playListService.deletePlayListByIdAndEditor("1", "1"));
  }

  @Test
  void testUpdatePlayList() {
    doNothing().when(playListRepository).updatePlayList(any());
    when(playListRepository.findPlayList(any())).thenReturn(new PlayList());
    assertDoesNotThrow(
        () -> playListService.updatePlaylist("1", "1", PlayListRequest.builder().build()));
  }
}
