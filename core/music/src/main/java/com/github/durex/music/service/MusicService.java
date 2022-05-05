package com.github.durex.music.service;

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
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
@Slf4j
public class MusicService {

  public static final String MUSIC_NOT_FOUND = "music not found";
  public static final String ID_IS_EMPTY = "id is empty";
  public static final String MUSIC_NOT_UPDATED = "music not updated";
  public static final String MUSIC_NOT_DELETED = "music not deleted";
  public static final String TITLE_IS_EMPTY = "title is empty";
  @Inject MusicRepository repository;

  public Music getMusicById(String id) {
    var realId =
        Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY, EMPTY_PARAM));
    return repository.findById(realId).orElseThrow(() -> new ApiException("", ENTITY_NOT_FOUND));
  }

  public List<Music> getMusicsByTitle(String title) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    var result = repository.findByTitle(realTitle);
    if (result.isEmpty()) throw new ApiException(MUSIC_NOT_FOUND);
    return result;
  }

  public List<Music> getMusicsByTitle(String title, WildCardType wildCardEnum) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    var result = repository.findByTitle(realTitle, wildCardEnum);
    if (result.isEmpty()) throw new ApiException(MUSIC_NOT_FOUND, ENTITY_NOT_FOUND);
    return result;
  }

  @Transactional
  public int createMusic(Music music) {
    var saved = repository.save(music);
    if (saved == 0) {
      throw new ApiException("music not created");
    } else return saved;
  }

  @Transactional
  public int[] createMusic(List<Music> musics) {
    var created = repository.save(musics);
    if (ArrayUtils.isEmpty(created)) {
      throw new ApiException("music not created", INSERT_ERROR);
    } else return created;
  }

  @Transactional
  public int updateMusic(Music music) {
    var updated = repository.update(music);
    if (updated == 0) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    } else return updated;
  }

  @Transactional
  public int[] updateMusic(List<Music> musics) {
    var updated = repository.update(musics);
    if (ArrayUtils.isEmpty(updated)) {
      throw new ApiException(MUSIC_NOT_UPDATED, UPDATE_ERROR);
    } else return updated;
  }

  @Transactional
  public int deleteMusicById(String id) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    var deleted = repository.deleteById(realId);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  @Transactional
  public int deleteMusicByTitle(String title) {
    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    var deleted = repository.deleteByTitle(realTitle);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  @Transactional
  public int deleteMusicByTitle(String title, WildCardType wildCardEnum) {

    var realTitle =
        Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY, EMPTY_PARAM));
    var deleted = repository.deleteByTitle(realTitle, wildCardEnum);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }

  @Transactional
  public int delete(List<String> musicIds) {
    var deleted = repository.delete(musicIds);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED, DELETE_ERROR);
    } else return deleted;
  }
}
