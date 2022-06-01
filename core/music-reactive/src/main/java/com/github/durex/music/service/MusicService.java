package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import io.smallrye.common.constraint.NotNull;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@ApplicationScoped
public class MusicService {

  @Inject MusicRepository repository;

  public Mono<Music> getMusicById(@NotNull String id) {
    return repository
        .findById(id)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error finding music by id: " + id, ENTITY_NOT_FOUND);
            })
        .switchIfEmpty(
            Mono.error(new ApiException("Music not found by id:" + id, ENTITY_NOT_FOUND)));
  }

  public Flux<Music> getMusicsByTitle(@NotNull String title) {
    return repository
        .findByTitle(title)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error finding music by title: " + title, ENTITY_NOT_FOUND);
            })
        .switchIfEmpty(
            Flux.error(new ApiException("Music not found by title:" + title, ENTITY_NOT_FOUND)));
  }

  public Flux<Music> getMusicsByTitle(@NotNull String title, WildCardType wildCardEnum) {
    return repository
        .findByTitle(title, wildCardEnum)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error finding music by title: " + title, ENTITY_NOT_FOUND);
            })
        .switchIfEmpty(
            Flux.error(new ApiException("Music not found by title:" + title, ENTITY_NOT_FOUND)));
  }

  public Mono<Integer> createMusic(Music music) {
    return repository
        .save(music)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error saving music: " + music, SAVE_ERROR);
            });
  }

  public Flux<Integer> createMusic(List<Music> musics) {
    return repository
        .save(musics)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error saving music: " + musics, SAVE_ERROR);
            });
  }

  public Mono<Integer> updateMusic(Music music) {
    return repository
        .update(music)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error updating music: " + music, UPDATE_ERROR);
            });
  }

  public Flux<Integer> updateMusic(List<Music> musics) {
    return repository
        .update(musics)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error updating music: " + musics, UPDATE_ERROR);
            });
  }

  public Mono<Integer> deleteMusicById(@NotNull String id) {
    return repository
        .deleteById(id)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            });
  }

  public Mono<Integer> deleteMusicByTitle(@NotNull String title) {
    return repository
        .deleteByTitle(title)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            });
  }

  public Mono<Integer> deleteMusicByTitle(@NotNull String title, WildCardType wildCardEnum) {
    return repository
        .deleteByTitle(title, wildCardEnum)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            });
  }

  public Mono<Integer> delete(List<String> musicIds) {
    return repository
        .delete(musicIds)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            });
  }
}
