package com.github.durex.music.service;

import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.ENTITY_NOT_FOUND;
import static com.github.durex.shared.exceptions.model.ErrorCode.SAVE_ERROR;
import static com.github.durex.shared.exceptions.model.ErrorCode.UPDATE_ERROR;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.model.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistService {
  private static final String PLAYLIST_NOT_FOUND = "playlist not found";
  private static final String PLAYLIST_NOT_CREATED = "playlist not created";
  private static final String PLAYLIST_NOT_UPDATED = "playlist not updated";
  private static final String PLAYLIST_NOT_DELETED = "playlist not deleted";
  private static final String PLAYLIST_MUSIC_NOT_DELETED = "playlist music not deleted";

  protected final PlayListRepository repository;
  protected final PlayListMusicRepository playListMusicRepository;

  public PlaylistService(
      PlayListRepository repository, PlayListMusicRepository playListMusicRepository) {
    this.repository = repository;
    this.playListMusicRepository = playListMusicRepository;
  }

  public List<PlayList> findPlayListByTitle(String title) {
    return repository.findByTitle(title);
  }

  public List<PlayList> findPlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return repository.findByTitle(realTitle, wildcard);
  }

  public PlayList findPlayListById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ApiException(PLAYLIST_NOT_FOUND, ENTITY_NOT_FOUND));
  }

  public List<PlayList> findPlayList() {
    return repository.findAll();
  }

  public List<Integer> createPlaylist(PlayList playList, List<Music> musics) {
    requireAffected(repository.save(playList), PLAYLIST_NOT_CREATED, SAVE_ERROR);
    return Arrays.stream(playListMusicRepository.saveMusicsToPlayList(playList.getId(), musics))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  public List<Integer> createPlaylist(PlayListMusic playListMusic) {
    requireAffected(repository.save(playListMusic), PLAYLIST_NOT_CREATED, SAVE_ERROR);
    return Arrays.stream(
            playListMusicRepository.saveMusicsToPlayList(
                playListMusic.getId(), playListMusic.getMusics()))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  public Integer updatePlaylist(PlayList playList) {
    return requireAffected(repository.update(playList), PLAYLIST_NOT_UPDATED, UPDATE_ERROR);
  }

  public List<Integer> updatePlaylist(List<PlayList> playLists) {
    return Arrays.stream(repository.update(playLists))
        .boxed()
        .collect(Collectors.toUnmodifiableList());
  }

  public Integer deletePlaylistById(String id) {
    return requireAffected(repository.deleteById(id), PLAYLIST_NOT_DELETED, DELETE_ERROR);
  }

  public Integer deletePlayListByTitle(String title) {
    return requireAffected(repository.deleteByTitle(title), PLAYLIST_NOT_DELETED, DELETE_ERROR);
  }

  public Integer deletePlayListByTitle(String title, WildCardType wildcard) {
    var realTitle = SqlHelper.likeClauseBuilder(wildcard, title);
    return requireAffected(
        repository.deleteByTitle(realTitle, wildcard), PLAYLIST_NOT_DELETED, DELETE_ERROR);
  }

  public Integer deleteMusicFromPlayList(String id, List<String> musicIds) {
    return requireAffected(
        playListMusicRepository.deleteMusicFromPlayList(id, musicIds),
        PLAYLIST_MUSIC_NOT_DELETED,
        DELETE_ERROR);
  }

  public Integer clearMusicsFromPlayList(String playListId) {
    return requireAffected(
        playListMusicRepository.clearMusicsFromPlayList(playListId),
        PLAYLIST_MUSIC_NOT_DELETED,
        DELETE_ERROR);
  }

  private static int requireAffected(int affected, String message, ErrorCode errorCode) {
    if (affected == 0) {
      throw new ApiException(message, errorCode);
    }
    return affected;
  }
}
