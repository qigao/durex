package com.github.durex.music.service;

import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;

import com.github.durex.music.api.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import io.smallrye.common.constraint.NotNull;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
@Slf4j
public class MusicService {

  public static final String MUSIC_NOT_FOUND = "music not found";
  public static final String MUSIC_NOT_UPDATED = "music not updated";
  public static final String MUSIC_NOT_DELETED = "music not deleted";
  @Inject MusicRepository repository;

  public Music getMusicById(@NotNull String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND));
  }

  public List<Music> getMusicsByTitle(@NotNull String title) {

    var result = repository.findByTitle(title);
    if (result.isEmpty()) throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
    return result;
  }

  public List<Music> getMusicsByTitle(@NotNull String title, WildCardType wildCardEnum) {
    var result = repository.findByTitle(title, wildCardEnum);
    if (result.isEmpty()) throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
    return result;
  }

  public int createMusic(Music music) {
    var saved = repository.save(music);
    if (saved == 0) {
      throw new ApiException("music not created");
    } else return saved;
  }

  public int[] createMusic(List<Music> musics) {
    var created = repository.save(musics);
    if (ArrayUtils.isEmpty(created)) {
      throw new ApiException("music not created", SAVE_ERROR);
    } else return created;
  }

  public int updateMusic(Music music) {
    var updated = repository.update(music);
    if (updated == 0) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    } else return updated;
  }

  public int[] updateMusic(List<Music> musics) {
    var updated = repository.update(musics);
    if (ArrayUtils.isEmpty(updated)) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    } else return updated;
  }

  public int deleteMusicById(@NotNull String id) {
    var deleted = repository.deleteById(id);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  public int deleteMusicByTitle(@NotNull String title) {
    var deleted = repository.deleteByTitle(title);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  public int deleteMusicByTitle(@NotNull String title, WildCardType wildCardEnum) {
    var deleted = repository.deleteByTitle(title, wildCardEnum);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  public int delete(List<String> musicIds) {
    var deleted = repository.delete(musicIds);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }
}
