package com.github.durex.basic.service;

import com.github.durex.basic.entity.Music;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.repository.MusicRepository;
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
public class MusicService {

  @Inject MusicRepository musicRepository;

  public Music getMusicByIdAndEditor(String id, String editor) {
    return musicRepository
        .find("from Music where (editor is null or editor =?1) and id = ?2", editor, id)
        .stream()
        .findFirst()
        .orElseThrow(
            () -> {
              var audioError = String.format("Music id: %s, editor: %s not found", id, editor);
              log.error(audioError);
              return new NotFoundException(audioError);
            });
  }

  public List<Music> getMusicsByTitleAndEditor(String title, String editor, int start, int end) {
    if (title == null) {
      return getMusicsByEditor(editor, start, end);
    }
    return musicRepository
        .find(
            "from Music where (title like concat('%',?1,'%')) and (editor is null or editor =?2)",
            title, editor)
        .page(start, end)
        .list();
  }

  public List<Music> getMusicsByEditor(String editor, int start, int end) {
    return musicRepository
        .find("from Music where  editor =?1 or editor is null", editor)
        .page(start, end)
        .list();
  }

  @Transactional
  public Music createMusic(String editor, MusicRequest musicRequest) {
    var m3u8 = EntityMapper.musicRequestToEntity(musicRequest);
    m3u8.toBuilder().createTime(LocalDateTime.now()).editor(editor).build();
    musicRepository.persistAndFlush(m3u8);
    return m3u8;
  }

  @Transactional
  public Music updateMusic(String id, String editor, MusicRequest musicRequest) {
    var m3u8ToUpdate = getMusicByIdAndEditor(id, editor);
    log.info("find music by id: {}", m3u8ToUpdate);

    m3u8ToUpdate.setUpdateTime(LocalDateTime.now());
    m3u8ToUpdate.setArtist(musicRequest.getArtist());
    m3u8ToUpdate.setCoverUrl(musicRequest.getCoverUrl());
    m3u8ToUpdate.setDuration(musicRequest.getDuration());
    m3u8ToUpdate.setDescription(musicRequest.getDescription());
    m3u8ToUpdate.setPlayUrl(musicRequest.getPlayUrl());
    m3u8ToUpdate.setEditor(editor);
    m3u8ToUpdate.setChannels(musicRequest.getChannels());
    m3u8ToUpdate.setSampleRate(musicRequest.getSampleRate());
    m3u8ToUpdate.setTitle(musicRequest.getTitle());
    musicRepository.persist(m3u8ToUpdate);
    return m3u8ToUpdate;
  }

  @Transactional
  public void deleteMusic(String id, String editor) {
    var audioToUpdate = getMusicByIdAndEditor(id, editor);
    log.info("find music by id: {}", audioToUpdate);
    musicRepository.delete("from Music where id=?1", id);
  }
}
