package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.ID_IS_EMPTY;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;
import static com.github.durex.music.support.EntityConstants.TITLE_IS_EMPTY;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;

import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.api.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.utils.Helper;
import com.github.durex.sqlbuilder.SqlHelper;
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
public class PlaylistService {
  @Inject PlayListRepository repository;
  @Inject PlayListMusicRepository playListMusicRepository;

  public Flux<PlayList> findPlayListByTitle(@NotNull String title) {
    return Flux.from(repository.findByTitle(title))
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error find playlist by title : " + title, ENTITY_NOT_FOUND);
            })
        .doOnNext(playList -> log.info("find playlist by title with id: {}", playList.getId()));
  }

  public Flux<PlayList> findPlayListByTitle(@NotNull String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return repository
        .findByTitle(realTitle, wildcard)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              String message =
                  String.format("Error find playlist by title %s, wildcard: %s", title, wildcard);
              throw new ApiException(message, ENTITY_NOT_FOUND);
            })
        .doOnNext(playList -> log.info("find playlist by title with id: {}", playList.getId()));
  }

  public Mono<PlayList> findPlayListById(@NotNull String id) {
    return repository
        .findById(id)
        .doOnSuccess(playList -> log.info("find playlist by id: {}", playList.getId()))
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error find playlist by id: " + id, ENTITY_NOT_FOUND);
            });
  }

  public Flux<PlayList> findPlayList() {
    return repository
        .findAll()
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException("Error find all playlist");
            })
        .doOnComplete(() -> log.info("find all playlist"));
  }

  public Flux<Integer> createPlaylist(PlayList playList, List<Music> musics) {
    return repository
        .save(playList)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(
                  "Error occurred when saving playlist: " + playList.getTitle(), SAVE_ERROR);
            })
        .doOnSuccess(p -> log.info("created playlist with id: {}", playList.getId()))
        .flatMapMany(
            p ->
                playListMusicRepository
                    .saveMusicsToPlayList(playList.getId(), musics)
                    .doOnError(
                        e -> {
                          log.error(e.getMessage(), e);
                          throw new ApiException(
                              "Error occurred when saving musics to playlist: "
                                  + playList.getTitle(),
                              SAVE_ERROR);
                        }));
  }

  public Flux<Integer> createPlaylist(PlayListMusic playListMusic) {
    return repository
        .save(playListMusic)
        .doOnError(
            e1 -> {
              log.error(e1.getMessage(), e1);
              throw new ApiException("Error occurred when saving playlist");
            })
        .flatMapMany(
            p ->
                playListMusicRepository
                    .saveMusicsToPlayList(playListMusic.getId(), playListMusic.getMusics())
                    .doOnError(
                        e -> {
                          log.error(e.getMessage(), e);
                          throw new ApiException(
                              "Error occurred when saving musics to playlist", SAVE_ERROR);
                        }));
  }

  public Mono<Integer> updatePlaylist(PlayList playList) {
    return repository
        .update(playList)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(e.getMessage());
            });
  }

  public Flux<Integer> updatePlaylist(List<PlayList> playLists) {
    return repository
        .update(playLists)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_UPDATED);
            });
  }

  public Mono<Integer> deletePlaylistById(String id) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    return repository
        .deleteById(realId)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED);
            });
  }

  public Mono<Integer> deletePlayListByTitle(String title) {
    var realTitle = Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY));
    return repository
        .deleteByTitle(realTitle)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED);
            });
  }

  public Mono<Integer> deletePlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return repository
        .deleteByTitle(realTitle, wildcard)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED);
            });
  }

  public Mono<Integer> deleteMusicFromPlayList(String id, List<String> musicIds) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    return playListMusicRepository
        .deleteMusicFromPlayList(realId, musicIds)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED);
            });
  }

  public Mono<Integer> clearMusicsFromPlayList(String playListId) {
    var realId = Helper.makeOptional(playListId).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    return playListMusicRepository
        .clearMusicsFromPlayList(realId)
        .doOnError(
            e -> {
              log.error(e.getMessage(), e);
              throw new ApiException(MUSIC_NOT_DELETED);
            });
  }
}
