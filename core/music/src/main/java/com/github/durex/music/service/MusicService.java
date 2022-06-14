package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;
import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
@Slf4j
public class MusicService {
  @Inject MusicRepository repository;

  /**
   * get a music by id.
   *
   * @param id id of the music, must not be null, empty or blank, if not found, throw ApiException
   * @return {@link Music}
   */
  public Music getMusicById(@NonNull String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND));
  }

  /**
   * get musics by title.
   *
   * @param title title of the music, must not be null, empty or blank if not found, throw
   *     ApiException
   * @return musics, List<{@link Music}>
   */
  public List<Music> getMusicsByTitle(@NonNull String title) {
    var result = repository.findByTitle(title);
    if (isEmpty(result)) {
      throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
    }
    return result;
  }

  /**
   * find music by title, title can be like '%title%' or 'title%' or '%title'.
   *
   * @param title title of the music, must not be null, empty or blank, if not found, throw
   *     ApiException
   * @param wildCardEnum {@link WildCardType}
   * @return musics, List<{@link Music}>
   */
  public List<Music> getMusicsByTitle(@NonNull String title, WildCardType wildCardEnum) {
    var musics = repository.findByTitle(title, wildCardEnum);
    if (isEmpty(musics)) {
      throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
    }
    return musics;
  }

  public int createMusic(Music music) {
    var saved = repository.save(music);
    if (saved == 0) {
      throw new ApiException("music not created");
    }
    return saved;
  }

  public int[] createMusic(List<Music> musics) {
    var created = repository.save(musics);
    if (ArrayUtils.isEmpty(created)) {
      throw new ApiException("music not created", SAVE_ERROR);
    }
    return created;
  }

  public int updateMusic(Music music) {
    var updated = repository.update(music);
    if (updated == 0) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    }
    return updated;
  }

  public int[] updateMusic(List<Music> musics) {
    var updated = repository.update(musics);
    if (ArrayUtils.isEmpty(updated)) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    }
    return updated;
  }

  public int deleteMusicById(@NonNull String id) {
    var deleted = repository.deleteById(id);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    }
    return deleted;
  }

  public int deleteMusicByTitle(@NonNull String title) {
    var deleted = repository.deleteByTitle(title);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    }
    return deleted;
  }

  public int deleteMusicByTitle(@NonNull String title, WildCardType wildCardEnum) {
    var deleted = repository.deleteByTitle(title, wildCardEnum);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    }
    return deleted;
  }

  public int delete(List<String> musicIds) {
    var deleted = repository.delete(musicIds);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    }
    return deleted;
  }
}
