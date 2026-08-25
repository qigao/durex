package com.github.durex.music.spring;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.model.PlayListMusic;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.service.PlaylistService;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class SpringPlaylistService extends PlaylistService {
  public SpringPlaylistService(
      PlayListRepository repository, PlayListMusicRepository playListMusicRepository) {
    super(repository, playListMusicRepository);
  }

  @Override
  @Transactional
  public List<Integer> createPlaylist(PlayList playList, List<Music> musics) {
    return super.createPlaylist(playList, musics);
  }

  @Override
  @Transactional
  public List<Integer> createPlaylist(PlayListMusic playListMusic) {
    return super.createPlaylist(playListMusic);
  }
}
