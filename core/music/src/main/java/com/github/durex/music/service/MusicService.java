package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.shared.annotation.NullChecker;
import com.github.durex.shared.annotation.ValueChecker;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

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
  public Music getMusicById(String id) {
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
  @NullChecker
  public List<Music> getMusicsByTitle(String title) {
    return repository.findByTitle(title);
  }

  /**
   * find music by title, title can be like '%title%' or 'title%' or '%title'.
   *
   * @param title title of the music, must not be null, empty or blank, if not found, throw
   *     ApiException
   * @param wildCardEnum {@link WildCardType}
   * @return musics, List<{@link Music}>
   */
  @NullChecker
  public List<Music> getMusicsByTitle(String title, WildCardType wildCardEnum) {
    return repository.findByTitle(title, wildCardEnum);
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer createMusic(Music music) {
    return repository.save(music);
  }

  @NullChecker
  public List<Integer> createMusic(List<Music> musics) {
    return Arrays.stream(repository.save(musics)).boxed().collect(Collectors.toUnmodifiableList());
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer updateMusic(Music music) {
    return repository.update(music);
  }

  @NullChecker
  public List<Integer> updateMusic(List<Music> musics) {
    return Arrays.stream(repository.update(musics))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer deleteMusicById(String id) {
    return repository.deleteById(id);
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer deleteMusicByTitle(String title) {
    return repository.deleteByTitle(title);
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer deleteMusicByTitle(String title, WildCardType wildCardEnum) {
    return repository.deleteByTitle(title, wildCardEnum);
  }

  @ValueChecker(value = "0", type = Integer.class)
  public Integer delete(List<String> musicIds) {
    return repository.delete(musicIds);
  }
}
