package com.github.durex.music.spring;

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.support.Helper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/music")
public class MusicHttpController {
  private final MusicService musicService;

  public MusicHttpController(MusicService musicService) {
    this.musicService = musicService;
  }

  @GetMapping("/{id}")
  public RespData<Music> getMusic(@PathVariable("id") String musicId) {
    return RespData.of(musicService.getMusicById(musicId), Helper.okResponse());
  }
}
