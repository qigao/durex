package com.github.durex.music.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@QuarkusTest
class PlaylistServiceTest {

  @InjectMock PlayListRepository repository;
  @Inject PlaylistService service;
  @InjectMock PlayListMusicRepository playListMusicRepository;

  @Test
  void testFindPlayListByTitle() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findByTitle("test")).thenReturn(Flux.fromIterable(playLists));
    service
        .findPlayListByTitle("test")
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  void testFindPlayLIstByTitleWithWildCard() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findByTitle(any(), any())).thenReturn(Flux.fromIterable(playLists));
    service
        .findPlayListByTitle("test", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  void testFindPlayListById() {
    Mockito.when(repository.findById(any())).thenReturn(Mono.just(DemoMusicData.givenAPlayList()));
    service
        .findPlayListById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testFindAllPlayList() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(playLists));
    service.findPlayList().as(StepVerifier::create).expectNextCount(5).verifyComplete();
  }

  @Test
  void testUpdatePlaylist() {
    Mockito.when(repository.update(any(PlayList.class))).thenReturn(Mono.just(1));
    service
        .updatePlaylist(DemoMusicData.givenAPlayList())
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
  }

  @Test
  void testUpdatePlaylistWithException() {
    Mockito.when(repository.update(any(PlayList.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    var playList = DemoMusicData.givenAPlayList();
    service
        .updatePlaylist(playList)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testUpdatePlayListInBatch() {
    Mockito.when(repository.update(anyList())).thenReturn(Flux.just(1, 1, 1));
    List<PlayList> playLists = DemoMusicData.givenSomePlayList(3);
    service.updatePlaylist(playLists).as(StepVerifier::create).expectNextCount(3).verifyComplete();
  }

  @Test
  void testUpdatePlayListInBatchWithException() {
    Mockito.when(repository.update(anyList())).thenReturn(Flux.error(new ApiException("error")));
    var playLists = DemoMusicData.givenSomePlayList(5);
    service
        .updatePlaylist(playLists)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testCreatePlaylist() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(Mono.just(1));
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(Flux.just(1, 1, 1, 1, 1));
    service
        .createPlaylist(DemoMusicData.givenAPlayList(), DemoMusicData.givenSomeMusics(5))
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  void testCreatePlaylistWithException() {
    Mockito.when(repository.save(any(PlayList.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    PlayList playList = DemoMusicData.givenAPlayList();
    List<Music> musics = DemoMusicData.givenSomeMusics(5);
    service
        .createPlaylist(playList, musics)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testCreatePlaylistWhenSaveMusicsToPlaylistHasException() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(Mono.just(1));
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(Flux.error(new ApiException("error")));
    PlayList playList = DemoMusicData.givenAPlayList();
    List<Music> musics = DemoMusicData.givenSomeMusics(5);
    service
        .createPlaylist(playList, musics)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testCreatePlayListFromPlayListMusic() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(Mono.just(1));
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(Flux.just(1, 1, 1, 1, 1));
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    service
        .createPlaylist(playlistMusic)
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  void testCreatePlayListFromPlayListMusicWhenCreateMusicError() {
    Mockito.when(repository.save(any(PlayList.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    service
        .createPlaylist(playlistMusic)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testCreatePlayListFromPlayListMusicWhenCreatePlayListError() {
    Mockito.when(repository.save(any(PlayList.class))).thenReturn(Mono.just(1));
    Mockito.when(playListMusicRepository.saveMusicsToPlayList(any(), any()))
        .thenReturn(Flux.error(new ApiException("error")));
    var playlistMusic = DemoMusicData.givenAPlayListMusic();
    service
        .createPlaylist(playlistMusic)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testDeletePlaylistById() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(Mono.just(1));
    service
        .deletePlaylistById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeletePlaylistByIdWithException() {
    Mockito.when(repository.deleteById(anyString()))
        .thenReturn(Mono.error(new ApiException("error")));
    String id = UniqID.getId();
    service
        .deletePlaylistById(id)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testDeletePlayListByTitle() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(Mono.just(1));
    service
        .deletePlayListByTitle(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeletePlayListByTitleWithException() {
    Mockito.when(repository.deleteByTitle(anyString()))
        .thenReturn(Mono.error(new ApiException("error")));
    String id = UniqID.getId();
    service
        .deletePlayListByTitle(id)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testDeletePlayListByTitleWithWildCard() {
    Mockito.when(repository.deleteByTitle(anyString(), any())).thenReturn(Mono.just(1));
    service
        .deletePlayListByTitle(UniqID.getId(), WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeletePlayLIstByTitleWithWildCardHasException() {
    Mockito.when(repository.deleteByTitle(anyString(), any()))
        .thenReturn(Mono.error(new ApiException("error")));
    String id = UniqID.getId();
    service
        .deletePlayListByTitle(id, WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testDeleteMusicFromPlayList() {
    Mockito.when(playListMusicRepository.deleteMusicFromPlayList(any(), anyList()))
        .thenReturn(Mono.just(1));
    service
        .deleteMusicFromPlayList(UniqID.getId(), List.of(UniqID.getId()))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeleteMusicFromPlayListWithException() {
    Mockito.when(playListMusicRepository.deleteMusicFromPlayList(any(), anyList()))
        .thenReturn(Mono.error(new ApiException("error")));
    String id = UniqID.getId();
    List<String> musicIds = List.of(UniqID.getId());
    service
        .deleteMusicFromPlayList(id, musicIds)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testClearMusicsFromPlayList() {
    Mockito.when(playListMusicRepository.clearMusicsFromPlayList(any())).thenReturn(Mono.just(1));
    service
        .clearMusicsFromPlayList(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testClearMusicsFromPlayListWithException() {
    Mockito.when(playListMusicRepository.clearMusicsFromPlayList(any()))
        .thenReturn(Mono.error(new ApiException("error")));
    String id = UniqID.getId();
    service
        .clearMusicsFromPlayList(id)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }
}
