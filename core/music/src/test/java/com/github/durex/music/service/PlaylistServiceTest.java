package com.github.durex.music.service;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
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
class PlaylistServiceTest {

  @InjectMock PlayListRepository repository;
  @Inject PlaylistService service;
  @InjectMock PlayListMusicRepository playListMusicRepository;

  @Test
  void testFindPlayListByTitle() {
    Mockito.when(repository.findByTitle("test")).thenReturn(DemoMusicData.givenSomePlayList(5));
    assertEquals(5, service.findPlayListByTitle("test").size());
  }

  @Test
  void testFindPlayLIstByTitleWithWildCard() {
    Mockito.when(repository.findByTitle(any(), any()))
        .thenReturn(DemoMusicData.givenSomePlayList(5));
    assertEquals(5, service.findPlayListByTitle("test", WildCardType.CONTAINS).size());
  }

  @Test
  void testFindPlayListByTitleWithWildCardHasException() {
    Mockito.when(repository.findByTitle(any(), any())).thenReturn(emptyList());
    assertThrows(
        ApiException.class, () -> service.findPlayListByTitle("test", WildCardType.CONTAINS));
  }

  @Test
  void testFindPlayListById() {
    Mockito.when(repository.findById(any()))
        .thenReturn(Optional.ofNullable(DemoMusicData.givenAPlayList()));
    assertNotNull(service.findPlayListById(UniqID.getId()).getId());
  }

  @Test
  void testFindPlayListByIdWithException() {
    Mockito.when(repository.findById(any())).thenReturn(Optional.empty());
    assertThrows(ApiException.class, () -> service.findPlayListById("1234"));
  }

  @Test
  void testFindAllPlayList() {
    Mockito.when(repository.findAll()).thenReturn(DemoMusicData.givenSomePlayList(5));
    assertEquals(5, service.findPlayList().size());
  }

  @Test
  void testUpdatePlaylist() {
    Mockito.when(repository.update(any(PlayList.class))).thenReturn(1);
    assertEquals(1, service.updatePlaylist(DemoMusicData.givenAPlayList()));
  }

  @Test
  void testUpdatePlaylistWithException() {
    Mockito.when(repository.update(any(PlayList.class))).thenReturn(0);
    var playList = DemoMusicData.givenAPlayList();
    assertThrows(ApiException.class, () -> service.updatePlaylist(playList));
  }

  @Test
  void testUpdatePlayListInBatch() {
    Mockito.when(repository.update(anyList())).thenReturn(new int[] {2, 2, 3});
    List<PlayList> playLists = DemoMusicData.givenSomePlayList(5);
    assertEquals(3, service.updatePlaylist(playLists).size());
  }

  @Test
  void testUpdatePlayListInBatchWithException() {
    Mockito.when(repository.update(anyList())).thenReturn(ArrayUtils.EMPTY_INT_ARRAY);
    var playLists = DemoMusicData.givenSomePlayList(5);
    assertThrows(ApiException.class, () -> service.updatePlaylist(playLists));
  }

  @Test
  void testCreatePlaylist() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(1);
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(new int[] {1, 2, 3, 4, 5});
    var result =
        service.createPlaylist(DemoMusicData.givenAPlayList(), DemoMusicData.givenSomeMusics(5));
    assertEquals(5, result.size());
  }

  @Test
  void testCreatePlaylistWithException() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(0);
    PlayList playList = DemoMusicData.givenAPlayList();
    List<Music> musics = DemoMusicData.givenSomeMusics(5);
    assertThrows(ApiException.class, () -> service.createPlaylist(playList, musics));
  }

  @Test
  void testCreatePlaylistWhenSaveMusicsToPlaylistHasException() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(1);
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(ArrayUtils.EMPTY_INT_ARRAY);
    PlayList playList = DemoMusicData.givenAPlayList();
    List<Music> musics = DemoMusicData.givenSomeMusics(5);
    assertThrows(ApiException.class, () -> service.createPlaylist(playList, musics));
  }

  @Test
  void testCreatePlayListFromPlayListMusic() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(1);
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(new int[] {1, 2, 3, 4, 5});
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    var result = service.createPlaylist(playlistMusic);
    assertEquals(5, result.size());
  }

  @Test
  void testCreatePlayListFromPlayListMusicWhenCreateMusicError() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(0);
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    assertThrows(ApiException.class, () -> service.createPlaylist(playlistMusic));
  }

  @Test
  void testCreatePlayListFromPlayListMusicWhenCreatePlayListError() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(1);
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(ArrayUtils.EMPTY_INT_ARRAY);
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    assertThrows(ApiException.class, () -> service.createPlaylist(playlistMusic));
  }

  @Test
  void testDeletePlaylistById() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(1);
    assertEquals(1, service.deletePlaylistById(UniqID.getId()));
  }

  @Test
  void testDeletePlaylistByIdWithException() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(0);
    String id = UniqID.getId();
    assertThrows(ApiException.class, () -> service.deletePlaylistById(id));
  }

  @Test
  void testDeletePlayListByTitle() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(1);
    assertEquals(1, service.deletePlayListByTitle(UniqID.getId()));
  }

  @Test
  void testDeletePlayListByTitleWithException() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(0);
    String id = UniqID.getId();
    assertThrows(ApiException.class, () -> service.deletePlayListByTitle(id));
  }

  @Test
  void testDeletePlayListByTitleWithWildCard() {
    Mockito.when(repository.deleteByTitle(anyString(), any())).thenReturn(1);
    assertEquals(1, service.deletePlayListByTitle(UniqID.getId(), WildCardType.CONTAINS));
  }

  @Test
  void testDeletePlayLIstByTitleWithWildCardHasException() {
    Mockito.when(repository.deleteByTitle(anyString(), any())).thenReturn(0);
    String id = UniqID.getId();
    assertThrows(
        ApiException.class, () -> service.deletePlayListByTitle(id, WildCardType.CONTAINS));
  }

  @Test
  void testDeleteMusicFromPlayList() {
    Mockito.when(playListMusicRepository.deleteMusicFromPlayList(any(), anyList())).thenReturn(1);
    assertEquals(1, service.deleteMusicFromPlayList(UniqID.getId(), List.of(UniqID.getId())));
  }

  @Test
  void testDeleteMusicFromPlayListWithException() {
    Mockito.when(playListMusicRepository.deleteMusicFromPlayList(any(), anyList())).thenReturn(0);
    String id = UniqID.getId();
    List<String> musicIds = List.of(UniqID.getId());
    assertThrows(ApiException.class, () -> service.deleteMusicFromPlayList(id, musicIds));
  }

  @Test
  void testClearMusicsFromPlayList() {
    Mockito.when(playListMusicRepository.clearMusicsFromPlayList(any())).thenReturn(1);
    assertEquals(1, service.clearMusicsFromPlayList(UniqID.getId()));
  }

  @Test
  void testClearMusicsFromPlayListWithException() {
    Mockito.when(playListMusicRepository.clearMusicsFromPlayList(any())).thenReturn(0);
    String id = UniqID.getId();
    assertThrows(ApiException.class, () -> service.clearMusicsFromPlayList(id));
  }
}
