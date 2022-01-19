package com.github.durex.music.service;

import com.github.durex.music.entity.Music;
import com.github.durex.music.model.MusicRequest;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.util.EntityMapper;
import java.time.LocalDateTime;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class MusicService {

  @Inject MusicRepository musicRepository;

  public Music getMusicByIdAndEditor(String id, String editor) {
    return musicRepository.findMusicsByIdAndEditor(id, editor);
  }

  public List<Music> getMusicsByEditorAndDevice(String editor, String device, int start, int end) {
    return musicRepository.findMusicByEditorAndDeviceWithPage(editor, device, start, end);
  }

  public List<Music> getMusicsByTitleAndEditor(String title, String editor, int start, int end) {
    if (title == null || title.isEmpty()) {
      return musicRepository.findMusicsByEditor(editor, start, end);
    }
    return musicRepository.findMusicsByTitleAndEditorWithPage(title, editor, start, end);
  }

  public List<Music> getMusicsByEditor(String editor, int start, int end) {
    return musicRepository.findMusicsByEditor(editor, start, end);
  }

  public void createMusic(String editor, MusicRequest musicRequest) {
    var m3u8 = EntityMapper.musicRequestToEntity(musicRequest);
    m3u8.setCreateTime(LocalDateTime.now());
    if (editor != null) {
      m3u8.setEditor(editor);
    }
    musicRepository.createMusic(m3u8);
  }

  public void updateMusic(String id, String editor, MusicRequest musicRequest) {
    var musicByIdAndEditor = getMusicByIdAndEditor(id, editor);
    log.info("find music by id: {}", musicByIdAndEditor);

    musicByIdAndEditor.setUpdateTime(LocalDateTime.now());
    musicByIdAndEditor.setArtist(musicRequest.getArtist());
    musicByIdAndEditor.setCoverUrl(musicRequest.getCoverUrl());
    musicByIdAndEditor.setDuration(musicRequest.getDuration());
    musicByIdAndEditor.setDescription(musicRequest.getDescription());
    musicByIdAndEditor.setPlayUrl(musicRequest.getPlayUrl());
    musicByIdAndEditor.setEditor(editor);
    musicByIdAndEditor.setChannels(musicRequest.getChannels());
    musicByIdAndEditor.setSampleRate(musicRequest.getSampleRate());
    musicByIdAndEditor.setTitle(musicRequest.getTitle());
    musicByIdAndEditor.setDevice(musicRequest.getDevice());
    musicByIdAndEditor.setStoryId(musicRequest.getStoryId());

    musicRepository.updateMusic(musicByIdAndEditor);
  }

  public void deleteMusic(String id, String editor) {
    var audioToUpdate = getMusicByIdAndEditor(id, editor);
    log.info("Deleting music by id: {}", audioToUpdate);
    musicRepository.deleteMusicById(id);
  }
}
