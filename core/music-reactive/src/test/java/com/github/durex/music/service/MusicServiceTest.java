package com.github.durex.music.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import com.github.durex.music.api.Music;
import com.github.durex.music.repository.MusicRepository;
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
class MusicServiceTest {

  @InjectMock MusicRepository repository;
  @Inject MusicService service;

  @Test
  void testGetMusicById() {
    Mockito.when(repository.findById(anyString()))
        .thenReturn(Mono.just(DemoMusicData.givenAMusic()));
    service
        .getMusicById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testGetMusicById_NotFound() {
    Mockito.when(repository.findById(anyString()))
        .thenReturn(Mono.error(new ApiException("error")));
    String music_id = UniqID.getId();
    service.getMusicById(music_id).as(StepVerifier::create).verifyError(ApiException.class);
  }

  @Test
  void testGetMusicsByTitle() {
    var musics = DemoMusicData.givenSomeMusics(5);
    Mockito.when(repository.findByTitle(anyString())).thenReturn(Flux.fromIterable(musics));
    service.getMusicsByTitle("title").as(StepVerifier::create).expectNextCount(5).verifyComplete();
  }

  @Test
  void testGetMusicsByTitle_NotFound() {
    Mockito.when(repository.findByTitle(anyString()))
        .thenReturn(Flux.error(new ApiException("error")));
    service
        .getMusicsByTitle("title")
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testGetMusicsByTitleWithWildCard() {
    var musics = DemoMusicData.givenSomeMusics(5);
    Mockito.when(repository.findByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(Flux.fromIterable(musics));
    service
        .getMusicsByTitle("title", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(5)
        .verifyComplete();
  }

  @Test
  void testGetMusicsByTitleWithWildCard_NotFound() {
    Mockito.when(repository.findByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(Flux.error(new ApiException("error")));
    service
        .getMusicsByTitle("title", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testCreateMusic() {
    Mockito.when(repository.save(any(Music.class))).thenReturn(Mono.just(1));
    service
        .createMusic(DemoMusicData.givenAMusic())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testCreateMusicWithException() {
    Mockito.when(repository.save(any(Music.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    var music = DemoMusicData.givenAMusic();
    service.createMusic(music).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testCreateMusicInBatch() {
    Mockito.when(repository.save(anyList())).thenReturn(Flux.just(1, 1, 1));
    var musics = DemoMusicData.givenSomeMusics(3);
    service.createMusic(musics).as(StepVerifier::create).expectNextCount(3).verifyComplete();
  }

  @Test
  void testCreateMusicInBatchWithException() {
    Mockito.when(repository.save(anyList())).thenReturn(Flux.error(new ApiException("error")));
    var musics = DemoMusicData.givenSomeMusics(3);
    service.createMusic(musics).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testUpdateMusic() {
    Mockito.when(repository.update(any(Music.class))).thenReturn(Mono.just(1));
    service
        .updateMusic(DemoMusicData.givenAMusic())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testUpdateMusicWithException() {
    Mockito.when(repository.update(any(Music.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    var music = DemoMusicData.givenAMusic();
    service.updateMusic(music).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testUpdateMusicInBatch() {
    Mockito.when(repository.update(anyList())).thenReturn(Flux.just(1, 1, 1));
    service
        .updateMusic(List.of(DemoMusicData.givenAMusic()))
        .as(StepVerifier::create)
        .expectNextCount(3)
        .verifyComplete();
  }

  @Test
  void testUpdateMusicInBatchWithException() {
    Mockito.when(repository.update(anyList())).thenReturn(Flux.error(new ApiException("error")));
    var musics = DemoMusicData.givenSomeMusics(3);
    service.updateMusic(musics).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testDeleteMusicById() {
    Mockito.when(repository.deleteById(anyString())).thenReturn(Mono.just(1));
    service
        .deleteMusicById(UniqID.getId())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeleteMusicByIdWithException() {
    Mockito.when(repository.deleteById(anyString()))
        .thenReturn(Mono.error(new ApiException("error")));
    var id = UniqID.getId();

    service.deleteMusicById(id).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testDeleteMusicInBatch() {
    Mockito.when(repository.delete(anyList())).thenReturn(Mono.just(10));
    service
        .delete(List.of(UniqID.getId()))
        .as(StepVerifier::create)
        .expectNext(10)
        .verifyComplete();
  }

  @Test
  void testDeleteMusicInBatchWithException() {
    Mockito.when(repository.delete(anyList())).thenReturn(Mono.error(new ApiException("error")));
    var ids = List.of(UniqID.getId());
    service.delete(ids).as(StepVerifier::create).expectError(ApiException.class).verify();
  }

  @Test
  void testDeleteMusicByTitle() {
    Mockito.when(repository.deleteByTitle(anyString())).thenReturn(Mono.just(1));
    service
        .deleteMusicByTitle("title")
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeleteMusicByTitleWithException() {
    Mockito.when(repository.deleteByTitle(anyString()))
        .thenReturn(Mono.error(new ApiException("error")));
    service
        .deleteMusicByTitle("title")
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }

  @Test
  void testDeleteMusicByTitleWithWildCard() {
    Mockito.when(repository.deleteByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(Mono.just(1));
    service
        .deleteMusicByTitle("title", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void testDeleteMusicByTitleWithWildCardWithException() {
    Mockito.when(repository.deleteByTitle(anyString(), any(WildCardType.class)))
        .thenReturn(Mono.error(new ApiException("error")));
    service
        .deleteMusicByTitle("title", WildCardType.CONTAINS)
        .as(StepVerifier::create)
        .expectError(ApiException.class)
        .verify();
  }
}
