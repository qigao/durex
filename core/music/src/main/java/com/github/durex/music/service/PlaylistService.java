package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.model.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.shared.annotation.NullChecker;
import com.github.durex.shared.annotation.ValueChecker;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PlaylistService {
  @Inject PlayListRepository repository;
  @Inject PlayListMusicRepository playListMusicRepository;

  @NullChecker
  public List<PlayList> findPlayListByTitle(String title) {
    return repository.findByTitle(title);
  }

  @NullChecker
  public List<PlayList> findPlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return repository.findByTitle(realTitle, wildcard);
  }

  @NullChecker
  public PlayList findPlayListById(String id) {
    return repository.findById(id).orElseThrow(() -> new ApiException("PlayList not found"));
  }

  @NullChecker
  public List<PlayList> findPlayList() {
    return repository.findAll();
  }

  @Transactional
  @NullChecker
  public List<Integer> createPlaylist(PlayList playList, List<Music> musics) {
    var created = repository.save(playList);
    if (created == 0) {
      throw new ApiException(MUSIC_NOT_FOUND);
    } else {
      return Arrays.stream(playListMusicRepository.saveMusicsToPlayList(playList.getId(), musics))
          .boxed()
          .collect(Collectors.toUnmodifiableList());
    }
  }

  @Transactional
  @NullChecker
  public List<Integer> createPlaylist(PlayListMusic playListMusic) {
    var created = repository.save(playListMusic);
    if (created == 0) {
      throw new ApiException(MUSIC_NOT_FOUND);
    } else {
      return Arrays.stream(
              playListMusicRepository.saveMusicsToPlayList(
                  playListMusic.getId(), playListMusic.getMusics()))
          .boxed()
          .collect(Collectors.toUnmodifiableList());
    }
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer updatePlaylist(PlayList playList) {
    var updated = repository.update(playList);
    if (updated == 0) {
      throw new ApiException(MUSIC_NOT_UPDATED);
    } else return updated;
  }

  @Transactional
  @NullChecker
  public List<Integer> updatePlaylist(List<PlayList> playLists) {
    return Arrays.stream(repository.update(playLists))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer deletePlaylistById(String id) {
    return repository.deleteById(id);
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer deletePlayListByTitle(String title) {
    return repository.deleteByTitle(title);
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer deletePlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return repository.deleteByTitle(realTitle, wildcard);
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer deleteMusicFromPlayList(String id, List<String> musicIds) {
    return playListMusicRepository.deleteMusicFromPlayList(id, musicIds);
  }

  @Transactional
  @ValueChecker(value = "0", type = Integer.class)
  public Integer clearMusicsFromPlayList(String playListId) {
    return playListMusicRepository.clearMusicsFromPlayList(playListId);
  }
}
