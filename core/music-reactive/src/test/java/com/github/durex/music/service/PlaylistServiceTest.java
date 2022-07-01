package com.github.durex.music.service;

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
import com.github.durex.uniqid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("When find playlist by title")
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
  @DisplayName("When find playlist by title return empty")
  void testFindPlayListByTitleReturnEmpty() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findByTitle("test")).thenReturn(Flux.empty());
    service
        .findPlayListByTitle("test")
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find playlist by title return exception")
  void testFindPlayListByTitleReturnException() {
    Mockito.when(repository.findByTitle("test"))
        .thenReturn(Flux.error(new ApiException("not found")));
    service
        .findPlayListByTitle("test")
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find playlist by title with wildcard")
  void testFindPlayListByTitleWithWildCard() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findByTitle(any(), any())).thenReturn(Flux.fromIterable(playLists));
    service
        .findPlayListByTitle("test", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  @DisplayName("When find playlist by titile with wildcard return exception")
  void testFindPlayListByTitleWithWildCardException() {
    Mockito.when(repository.findByTitle(any(), any()))
        .thenReturn(Flux.error(new ApiException("not found")));
    service
        .findPlayListByTitle("test", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find playlist by titile with wildcard return empty")
  void testFindPlayListByTitleWithWildCardReturnEmpty() {
    Mockito.when(repository.findByTitle(any(), any())).thenReturn(Flux.empty());
    service
        .findPlayListByTitle("test", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find playlist by id")
  void testFindPlayListById() {
    Mockito.when(repository.findById(any())).thenReturn(Mono.just(DemoMusicData.givenAPlayList()));
    service
        .findPlayListById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When find playlist by id return exception")
  void testFindPlayListByIdWithException() {
    Mockito.when(repository.findById(any())).thenReturn(Mono.error(new ApiException("not found")));
    service
        .findPlayListById(UniqID.getId())
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find playlist by id return empty")
  void testFindPlayListByIdReturnEmpty() {
    Mockito.when(repository.findById(any())).thenReturn(Mono.empty());
    service
        .findPlayListById(UniqID.getId())
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  @DisplayName("When find all playlist")
  void testFindAllPlayList() {
    var playLists = DemoMusicData.givenSomePlayList(5);
    Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(playLists));
    service.findPlayList().as(StepVerifier::create).expectNextCount(5).verifyComplete();
  }

  @Test
  @DisplayName("When find all playlist return exception")
  void testFindAllPlayListReturnException() {
    Mockito.when(repository.findAll()).thenReturn(Flux.error(new ApiException("not found")));
    service.findPlayList().as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  @DisplayName("When find all playlist and not found")
  void testFindAllPlayListWithException() {
    Mockito.when(repository.findAll()).thenReturn(Flux.empty());
    service.findPlayList().as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  @DisplayName("When update playlist")
  void testUpdatePlaylist() {
    Mockito.when(repository.update(any(PlayList.class))).thenReturn(Mono.just(1));
    service
        .updatePlaylist(DemoMusicData.givenAPlayList())
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When update playlist got exception")
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
  @DisplayName("When update playlist in bulk")
  void testUpdatePlayListInBatch() {
    Mockito.when(repository.update(anyList())).thenReturn(Flux.just(1, 1, 1));
    List<PlayList> playLists = DemoMusicData.givenSomePlayList(3);
    service.updatePlaylist(playLists).as(StepVerifier::create).expectNextCount(3).verifyComplete();
  }

  @Test
  @DisplayName("When update playlist in bulk got exception")
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
  @DisplayName("When create playlist")
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
  @DisplayName("When create playlist got exception")
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
  @DisplayName("When create playlist then insert music got exception")
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
  @DisplayName("When create playlist by full playlist")
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
  @DisplayName("When create playlist by full playlist with playlist exception")
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
  @DisplayName("When create playlist by full playlist with music exception")
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
  @DisplayName("When delete playlist by id")
  void testDeletePlaylistById() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(Mono.just(1));
    service
        .deletePlaylistById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When delete playlist by id got exception")
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
  @DisplayName("When delete playlist by title")
  void testDeletePlayListByTitle() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(Mono.just(1));
    service
        .deletePlayListByTitle(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When delete playlist by title got exception")
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
  @DisplayName("When delete playlist by title with wildcard")
  void testDeletePlayListByTitleWithWildCard() {
    Mockito.when(repository.deleteByTitle(anyString(), any())).thenReturn(Mono.just(1));
    service
        .deletePlayListByTitle(UniqID.getId(), WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When delete playlist by title with wildcard got exception")
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
  @DisplayName("When delete musics from playlist")
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
  @DisplayName("When delete musics from playlist got exception")
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
  @DisplayName("When delete all musics from playlist")
  void testClearMusicsFromPlayList() {
    Mockito.when(playListMusicRepository.clearMusicsFromPlayList(any())).thenReturn(Mono.just(1));
    service
        .clearMusicsFromPlayList(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  @DisplayName("When delete all musics from playlist got exception")
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
