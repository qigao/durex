package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_CREATED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;
import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MusicService {
  private final MusicRepository repository;

  public MusicService(MusicRepository repository) {
    this.repository = repository;
  }

  /**
   * get a music by id.
   *
   * @param id id of the music, must not be null, empty or blank, if not found, throw ApiException
   * @return {@link Music}
   */
  public Music getMusicById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND));
  }

  /**
   * get musics by title.
   *
   * @param title title of the music
   * @return matching musics, or an empty list when no music matches
   */
  public List<Music> getMusicsByTitle(String title) {
    return repository.findByTitle(title);
  }

  /**
   * find music by title, title can be like '%title%' or 'title%' or '%title'.
   *
   * @param title title of the music
   * @param wildCardEnum {@link WildCardType}
   * @return matching musics, or an empty list when no music matches
   */
  public List<Music> getMusicsByTitle(String title, WildCardType wildCardEnum) {
    return repository.findByTitle(title, wildCardEnum);
  }

  public Integer createMusic(Music music) {
    return requireAffected(repository.save(music), MUSIC_NOT_CREATED, SAVE_ERROR);
  }

  public List<Integer> createMusic(List<Music> musics) {
    return Arrays.stream(repository.save(musics)).boxed().collect(Collectors.toUnmodifiableList());
  }

  public Integer updateMusic(Music music) {
    return requireAffected(repository.update(music), MUSIC_NOT_UPDATED, UPDATE_ERROR);
  }

  public List<Integer> updateMusic(List<Music> musics) {
    return Arrays.stream(repository.update(musics))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  public Integer deleteMusicById(String id) {
    return requireAffected(repository.deleteById(id), MUSIC_NOT_DELETED, DELETE_ERROR);
  }

  public Integer deleteMusicByTitle(String title) {
    return requireAffected(repository.deleteByTitle(title), MUSIC_NOT_DELETED, DELETE_ERROR);
  }

  public Integer deleteMusicByTitle(String title, WildCardType wildCardEnum) {
    return requireAffected(
        repository.deleteByTitle(title, wildCardEnum), MUSIC_NOT_DELETED, DELETE_ERROR);
  }

  public Integer delete(List<String> musicIds) {
    return requireAffected(repository.delete(musicIds), MUSIC_NOT_DELETED, DELETE_ERROR);
  }

  private static int requireAffected(int affected, String message, ErrorCode errorCode) {
    if (affected == 0) {
      throw new ApiException(message, errorCode);
    }
    return affected;
  }
}
