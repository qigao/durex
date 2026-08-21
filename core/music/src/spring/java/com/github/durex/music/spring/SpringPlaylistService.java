package com.github.durex.music.spring;

import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.service.PlaylistService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SpringPlaylistService extends PlaylistService {
  public SpringPlaylistService(
      PlayListRepository repository, PlayListMusicRepository playListMusicRepository) {
    super(repository, playListMusicRepository);
  }
}
