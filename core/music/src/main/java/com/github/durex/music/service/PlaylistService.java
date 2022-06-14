package com.github.durex.music.service;

import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_DELETED;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_FOUND;
import static com.github.durex.music.support.EntityConstants.MUSIC_NOT_UPDATED;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.model.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
@Slf4j
public class PlaylistService {
  @Inject PlayListRepository repository;
  @Inject PlayListMusicRepository playListMusicRepository;

  public List<PlayList> findPlayListByTitle(@NonNull String title) {
    return repository.findByTitle(title);
  }

  public List<PlayList> findPlayListByTitle(@NonNull String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    var result = repository.findByTitle(realTitle, wildcard);
    if (result.isEmpty()) {
      throw new ApiException("PlayList not found");
    } else return result;
  }

  public PlayList findPlayListById(@NonNull String id) {
    return repository.findById(id).orElseThrow(() -> new ApiException("PlayList not found"));
  }

  public List<PlayList> findPlayList() {
    return repository.findAll();
  }

  @Transactional
  public int[] createPlaylist(PlayList playList, List<Music> musics) {
    var created = repository.save(playList);
    if (created == 0) {
      throw new ApiException(MUSIC_NOT_FOUND);
    } else {
      var result = playListMusicRepository.saveMusicsToPlayList(playList.getId(), musics);
      if (ArrayUtils.isEmpty(result)) {
        throw new ApiException(MUSIC_NOT_FOUND);
      } else return result;
    }
  }

  @Transactional
  public int[] createPlaylist(PlayListMusic playListMusic) {
    var created = repository.save(playListMusic);
    if (created == 0) {
      throw new ApiException(MUSIC_NOT_FOUND);
    } else {
      var result =
          playListMusicRepository.saveMusicsToPlayList(
              playListMusic.getId(), playListMusic.getMusics());
      if (ArrayUtils.isEmpty(result)) {
        throw new ApiException(MUSIC_NOT_FOUND);
      } else return result;
    }
  }

  @Transactional
  public int updatePlaylist(PlayList playList) {
    var updated = repository.update(playList);
    if (updated == 0) {
      throw new ApiException(MUSIC_NOT_UPDATED);
    } else return updated;
  }

  @Transactional
  public int[] updatePlaylist(List<PlayList> playLists) {
    var updated = repository.update(playLists);
    if (ArrayUtils.isEmpty(updated)) {
      throw new ApiException(MUSIC_NOT_UPDATED);
    } else return updated;
  }

  @Transactional
  public int deletePlaylistById(@NonNull String id) {
    var deleted = repository.deleteById(id);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deletePlayListByTitle(@NonNull String title) {
    var deleted = repository.deleteByTitle(title);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deletePlayListByTitle(@NonNull String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    var deleted = repository.deleteByTitle(realTitle, wildcard);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deleteMusicFromPlayList(@NonNull String id, List<String> musicIds) {
    var deleted = playListMusicRepository.deleteMusicFromPlayList(id, musicIds);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int clearMusicsFromPlayList(@NonNull String playListId) {
    var deleted = playListMusicRepository.clearMusicsFromPlayList(playListId);
    if (deleted == 0) throw new ApiException(MUSIC_NOT_DELETED);
    else return deleted;
  }
}
