package com.github.durex.music.service;

import com.github.durex.music.entity.PlayList;
import com.github.durex.music.model.PlayListRequest;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.uuid.UniqID;
import java.time.LocalDateTime;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PlaylistService {
  @Inject PlayListRepository playlistRepository;
  @Inject PlayListMusicRepository playListMusicRepository;

  public List<PlayList> getPlayListByEditorAndTitle(
      String title, String editor, int start, int size) {
    return playlistRepository.findPlayListByEditorAndTitleWithPage(title, editor, start, size);
  }

  public PlayList findPlayListById(String id) {
    return playlistRepository.findPlayList(id);
  }

  public List<PlayList> findPlayListByEditorAndDevice(
      String editor, String device, int start, int size) {
    return playlistRepository.findPlayListByEditorAndDeviceWithPage(editor, device, start, size);
  }

  public void updatePlaylist(String id, String editor, PlayListRequest playList) {
    var playlistToUpdate = playlistRepository.findPlayList(id);

    playlistToUpdate.setUpdateTime(LocalDateTime.now());
    playlistToUpdate.setCoverUrl(playList.getCoverUrl());
    playlistToUpdate.setDescription(playList.getDescription());
    playlistToUpdate.setEditor(editor);
    playlistToUpdate.setTitle(playList.getTitle());
    playlistToUpdate.setDevice(playList.getDevice());
    log.info("Updating playlist {}", playlistToUpdate);
    playlistRepository.updatePlayList(playlistToUpdate);
  }

  @Transactional
  public void createPlaylistWithID(String editor, PlayListRequest playListRequest) {
    var playlistToCreate =
        PlayList.builder()
            .id(playListRequest.getId())
            .createTime(LocalDateTime.now())
            .coverUrl(playListRequest.getCoverUrl())
            .description(playListRequest.getDescription())
            .editor(editor)
            .title(playListRequest.getTitle())
            .device(playListRequest.getDevice())
            .build();
    log.info("Create playlist {}", playlistToCreate);
    playlistRepository.createPlayList(playlistToCreate);
  }

  @Transactional
  public String createPlaylist(String editor, PlayListRequest playListRequest) {
    var playListID = UniqID.getId();
    var playlistToCreate =
        PlayList.builder()
            .id(playListID)
            .createTime(LocalDateTime.now())
            .coverUrl(playListRequest.getCoverUrl())
            .description(playListRequest.getDescription())
            .editor(editor)
            .title(playListRequest.getTitle())
            .device(playListRequest.getDevice())
            .build();
    log.info("Create playlist {}", playlistToCreate);
    playlistRepository.createPlayList(playlistToCreate);
    return playListID;
  }

  public int deletePlaylist(String id) {
    log.info("Deleting playlist with id {}", id);
    return playlistRepository.deletePlayListById(id);
  }

  public int deletePlayListByIdAndEditor(String id, String editor) {
    log.info("Deleting playlist with editor {}", editor);
    return playlistRepository.deletePlayListByIdAndEditor(id, editor);
  }
}
