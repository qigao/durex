package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.ID_IS_EMPTY;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_CREATED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;
import static com.github.durex.music.support.EntityConstants.TITLE_IS_EMPTY;
import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.EMPTY_PARAM;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.INSERT_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;

import com.github.durex.music.api.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.utils.Helper;
import com.github.durex.sqlbuilder.enums.WildCardType;
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

  public Mono<Music> getMusicById(String id) {
    var realId =
        Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY, EMPTY_PARAM));
    return repository
        .findById(realId)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
            })
        .doOnSuccess(music -> log.info("music found: {}", music));
  }

  public Flux<Music> getMusicsByTitle(String title) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    return repository
        .findByTitle(realTitle)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
            })
        .doOnEach(musicSignal -> log.info("music found: {}", musicSignal.get()))
        .doOnComplete(() -> log.info("music found: {}", "complete"));
  }

  public Flux<Music> getMusicsByTitle(String title, WildCardType wildCardEnum) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    return repository
        .findByTitle(realTitle, wildCardEnum)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
            })
        .doOnEach(musicSignal -> log.info("music found: {}", musicSignal.get()))
        .doOnComplete(() -> log.info("music found: {}", "complete"));
  }

  public Mono<Integer> createMusic(Music music) {
    return repository
        .save(music)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_CREATED, INSERT_ERROR);
            })
        .doOnSuccess(m -> log.info("music saved: {}", m));
  }

  public Flux<Integer> createMusic(List<Music> musics) {
    return repository
        .save(musics)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_CREATED, INSERT_ERROR);
            })
        .doOnEach(m -> log.info("music saved: {}", m))
        .doOnComplete(() -> log.info("music saved: {}", "complete"));
  }

  public Mono<Integer> updateMusic(Music music) {
    return repository
        .update(music)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
            })
        .doOnSuccess(m -> log.info("music updated: {}", m));
  }

  public Flux<Integer> updateMusic(List<Music> musics) {
    return repository
        .update(musics)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
            })
        .doOnEach(m -> log.info("music updated: {}", m))
        .doOnComplete(() -> log.info("music updated: {}", "complete"));
  }

  public Mono<Integer> deleteMusicById(String id) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    return repository
        .deleteById(realId)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            })
        .doOnSuccess(m -> log.info("music deleted: {}", m));
  }

  public Mono<Integer> deleteMusicByTitle(String title) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    return repository
        .deleteByTitle(realTitle)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            })
        .doOnSuccess(m -> log.info("music deleted: {}", m));
  }

  public Mono<Integer> deleteMusicByTitle(String title, WildCardType wildCardEnum) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    return repository
        .deleteByTitle(realTitle, wildCardEnum)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
            })
        .doOnSuccess(m -> log.info("music deleted: {}", m));
  }

  public Mono<Integer> delete(List<String> musicIds) {
    return repository
        .delete(musicIds)
        .doOnError(e -> log.error(e.getMessage(), e))
        .doOnSuccess(m -> log.info("music deleted: {}", m));
  }
}
