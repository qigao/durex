package com.github.durex.music.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class MusicServiceTest {

  @InjectMock MusicRepository repository;
  @Inject MusicService service;

  @Test
  void testGetMusicById() {
    Mockito.when(repository.findById(anyString()))
        .thenReturn(Optional.of(DemoMusicData.givenAMusic()));
    var result = service.getMusicById(UniqID.getId());
    assertNotNull(result.getId());
  }

  @Test
  void testGetMusicById_NotFound() {
    Mockito.when(repository.findById(anyString())).thenReturn(Optional.empty());
    String music_id = UniqID.getId();
    assertThrows(ApiException.class, () -> service.getMusicById(music_id));
  }

  @Test
  void testGetMusicsByTitle() {
    Mockito.when(repository.findByTitle(anyString())).thenReturn(DemoMusicData.givenSomeMusics(5));
    var result = service.getMusicsByTitle("title");
    assertEquals(5, result.size());
  }

  @Test
  void testGetMusicsByTitle_NotFound() {
    Mockito.when(repository.findByTitle(anyString())).thenReturn(List.of());
    assertThrows(ApiException.class, () -> service.getMusicsByTitle("title"));
  }

  @Test
  void testGetMusicsByTitleWithWildCard() {
    Mockito.when(repository.findByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(DemoMusicData.givenSomeMusics(5));
    var result = service.getMusicsByTitle("title", WildCardType.CONTAINS);
    assertEquals(5, result.size());
  }

  @Test
  void testGetMusicsByTitleWithWildCard_NotFound() {
    Mockito.when(repository.findByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(List.of());
    assertThrows(
        ApiException.class, () -> service.getMusicsByTitle("title", WildCardType.CONTAINS));
  }

  @Test
  void testCreateMusic() {
    Mockito.when(repository.save(any(Music.class))).thenReturn(1);
    assertEquals(1, service.createMusic(DemoMusicData.givenAMusic()));
  }

  @Test
  void testCreateMusicWithException() {
    Mockito.when(repository.save(any(Music.class))).thenReturn(0);
    var music = DemoMusicData.givenAMusic();
    assertThrows(ApiException.class, () -> service.createMusic(music));
  }

  @Test
  void testCreateMusicInBatch() {
    Mockito.when(repository.save(anyList())).thenReturn(new int[] {1, 2, 3});
    assertEquals(3, service.createMusic(List.of(DemoMusicData.givenAMusic())).length);
  }

  @Test
  void testCreateMusicInBatchWithException() {
    Mockito.when(repository.save(anyList())).thenReturn(ArrayUtils.EMPTY_INT_ARRAY);
    var musics = List.of(DemoMusicData.givenAMusic());
    assertThrows(ApiException.class, () -> service.createMusic(musics));
  }

  @Test
  void testUpdateMusic() {
    Mockito.when(repository.update(any(Music.class))).thenReturn(1);
    assertEquals(1, service.updateMusic(DemoMusicData.givenAMusic()));
  }

  @Test
  void testUpdateMusicWithException() {
    Mockito.when(repository.update(any(Music.class))).thenReturn(0);
    var music = DemoMusicData.givenAMusic();
    assertThrows(ApiException.class, () -> service.updateMusic(music));
  }

  @Test
  void testUpdateMusicInBatch() {
    Mockito.when(repository.update(anyList())).thenReturn(new int[] {1, 2, 3});
    assertEquals(3, service.updateMusic(List.of(DemoMusicData.givenAMusic())).length);
  }

  @Test
  void testUpdateMusicInBatchWithException() {
    Mockito.when(repository.update(anyList())).thenReturn(ArrayUtils.EMPTY_INT_ARRAY);
    var musics = List.of(DemoMusicData.givenAMusic());
    assertThrows(ApiException.class, () -> service.updateMusic(musics));
  }

  @Test
  void testDeleteMusicById() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(1);
    assertEquals(1, service.deleteMusicById(UniqID.getId()));
  }

  @Test
  void testDeleteMusicByIdWithException() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(0);
    var id = UniqID.getId();
    assertThrows(
        ApiException.class,
        () -> {
          service.deleteMusicById(id);
        });
  }

  @Test
  void testDeleteMusicInBatch() {
    Mockito.when(repository.delete(anyList())).thenReturn(10);
    assertEquals(10, service.delete(List.of(UniqID.getId())));
  }

  @Test
  void testDeleteMusicInBatchWithException() {
    Mockito.when(repository.delete(anyList())).thenReturn(0);
    var ids = List.of(UniqID.getId());
    assertThrows(ApiException.class, () -> service.delete(ids));
  }

  @Test
  void testDeleteMusicByTitle() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(1);
    assertEquals(1, service.deleteMusicByTitle("title"));
  }

  @Test
  void testDeleteMusicByTitleWithException() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(0);
    assertThrows(ApiException.class, () -> service.deleteMusicByTitle("title"));
  }

  @Test
  void testDeleteMusicByTitleWithWildCard() {
    Mockito.when(repository.deleteByTitle(anyString(), any(WildCardType.class))).thenReturn(1);
    assertEquals(1, service.deleteMusicByTitle("title", WildCardType.CONTAINS));
  }

  @Test
  void testDeleteMusicByTitleWithWildCardWithException() {
    Mockito.when(repository.deleteByTitle(anyString(), any(WildCardType.class))).thenReturn(0);
    assertThrows(
        ApiException.class, () -> service.deleteMusicByTitle("title", WildCardType.CONTAINS));
  }
}
