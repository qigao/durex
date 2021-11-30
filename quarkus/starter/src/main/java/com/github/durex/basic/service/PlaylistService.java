package com.github.durex.basic.service;

import com.github.durex.basic.entity.Music;
import com.github.durex.basic.entity.PlayList;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.basic.repository.PlaylistRepository;
import com.github.durex.basic.util.EntityMapper;
import java.time.LocalDateTime;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PlaylistService {
  @Inject PlaylistRepository playlistRepository;

  public List<PlayList> getPlayListByEditorAndTitle(
      String title, String editor, int start, int end) {
    if (title == null) {
      return getPlayListByEditor(editor, start, end);
    }
    return playlistRepository
        .find(
            "from PlayList where title like concat('%',?1,'%') and (editor=?2 or editor is null)",
            title, editor)
        .page(start, end)
        .list();
  }

  public PlayList getPlaylist(String id) {
    return playlistRepository
        .find("from PlayList where id=?1", id)
        .firstResultOptional()
        .orElseThrow(
            () -> {
              var playlistError = String.format("Playlist with id: %s not found", id);
              log.error(playlistError);
              return new NotFoundException(playlistError);
            });
  }

  public List<PlayList> getPlayListByEditor(String editor, int start, int end) {
    return playlistRepository
        .find("from PlayList where editor=?1 or editor is null", editor)
        .page(start, end)
        .list();
  }

  @Transactional
  public PlayList updatePlaylist(String id, String editor, PlayListRequest playList) {
    var playlistToUpdate = getPlaylist(id);
    List<Music> musicEntityList = EntityMapper.musicEntityListMapper(playList);

    playlistToUpdate.setUpdateTime(LocalDateTime.now());
    playlistToUpdate.setCoverUrl(playList.getCoverUrl());
    playlistToUpdate.setDescription(playList.getDescription());
    playlistToUpdate.setEditor(editor);
    playlistToUpdate.setTitle(playList.getTitle());
    playlistToUpdate.setTotal(musicEntityList.size());
    playlistToUpdate.setDuration(musicEntityList.stream().mapToInt(Music::getDuration).sum());
    playlistToUpdate.setMusics(musicEntityList);

    log.info("Updating playlist {}", playlistToUpdate);
    playlistRepository.persist(playlistToUpdate);
    return playlistToUpdate;
  }

  @Transactional
  public PlayList createPlaylist(String editor, PlayListRequest playListRequest) {
    List<Music> musicEntityList = EntityMapper.musicEntityListMapper(playListRequest);
    var playlistToCreate =
        PlayList.builder()
            .id(playListRequest.getId())
            .createTime(LocalDateTime.now())
            .coverUrl(playListRequest.getCoverUrl())
            .description(playListRequest.getDescription())
            .editor(editor)
            .title(playListRequest.getTitle())
            .total(musicEntityList.size())
            .duration(musicEntityList.stream().mapToInt(Music::getDuration).sum())
            .musics(musicEntityList)
            .build();
    log.info("Create playlist {}", playlistToCreate);
    playlistRepository.persistAndFlush(playlistToCreate);
    return playlistToCreate;
  }

  @Transactional
  public void deletePlaylist(String id) {
    playlistRepository.delete("from PlayList where id=?1", id);
  }
}