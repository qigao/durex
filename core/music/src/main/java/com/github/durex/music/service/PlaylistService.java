package com.github.durex.music.service;

import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.api.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.utils.Helper;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@ApplicationScoped
@Slf4j
public class PlaylistService {
  @Inject PlayListRepository repository;
  @Inject PlayListMusicRepository playListMusicRepository;
  @Inject TransactionManager transactionManager;
  public static final String MUSIC_NOT_FOUND = "music not found";
  public static final String ID_IS_EMPTY = "id is empty";
  public static final String MUSIC_NOT_UPDATED = "music not updated";
  public static final String MUSIC_NOT_DELETED = "music not deleted";
  public static final String TITLE_IS_EMPTY = "title is empty";

  public List<PlayList> findPlayListByTitle(String title) {
    var realTitle = Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY));
    return repository.findByTitle(realTitle);
  }

  public List<PlayList> findPlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    var result = repository.findByTitle(realTitle, wildcard);
    if (result.isEmpty()) {
      throw new ApiException("PlayList not found");
    } else return result;
  }

  public PlayList findPlayListById(String id) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    return repository.findById(realId).orElseThrow(() -> new ApiException("PlayList not found"));
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
  public int deletePlaylistById(String id) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    var deleted = repository.deleteById(realId);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deletePlayListByTitle(String title) {
    var realTitle = Helper.makeOptional(title).orElseThrow(() -> new ApiException(TITLE_IS_EMPTY));
    var deleted = repository.deleteByTitle(realTitle);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deletePlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    var deleted = repository.deleteByTitle(realTitle, wildcard);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int deleteMusicFromPlayList(String id, List<String> musicIds) {
    var realId = Helper.makeOptional(id).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    var deleted = playListMusicRepository.deleteMusicFromPlayList(realId, musicIds);
    if (deleted == 0) {
      throw new ApiException(MUSIC_NOT_DELETED);
    } else return deleted;
  }

  @Transactional
  public int clearMusicsFromPlayList(String playListId) {
    var realId = Helper.makeOptional(playListId).orElseThrow(() -> new ApiException(ID_IS_EMPTY));
    var deleted = playListMusicRepository.clearMusicsFromPlayList(realId);
    if (deleted == 0) throw new ApiException(MUSIC_NOT_DELETED);
    else return deleted;
  }
}
