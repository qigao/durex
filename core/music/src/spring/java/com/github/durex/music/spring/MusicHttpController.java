package com.github.durex.music.spring;

import com.github.durex.music.api.MusicApi;
import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.support.Helper;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MusicHttpController implements MusicApi {
  private final MusicService musicService;

  public MusicHttpController(MusicService musicService) {
    this.musicService = musicService;
  }

  @Override
  public RespData<List<Music>> list(String title, String musicId, int offset) {
    return RespData.of(musicService.getMusicsByTitle(title), Helper.okResponse());
  }

  @Override
  public RespData<Music> get(String musicId) {
    return RespData.of(musicService.getMusicById(musicId), Helper.okResponse());
  }

  @Override
  public RespData<Integer> create(Music music) {
    return RespData.of(musicService.createMusic(music), Helper.okResponse());
  }

  @Override
  public RespData<Integer> update(Music music) {
    return RespData.of(musicService.updateMusic(music), Helper.okResponse());
  }

  @Override
  public RespData<Integer> delete(String musicId) {
    return RespData.of(musicService.deleteMusicById(musicId), Helper.okResponse());
  }

  @GetMapping("/v1/music/")
  public RespData<List<Music>> listWithTrailingSlash(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "id", required = false) String musicId,
      @RequestParam(value = "offset", defaultValue = "10") int offset) {
    return list(title, musicId, offset);
  }

  @PostMapping("/v1/music/")
  public RespData<Integer> createWithTrailingSlash(@RequestBody Music music) {
    return create(music);
  }

  @PutMapping("/v1/music/")
  public RespData<Integer> updateWithTrailingSlash(@RequestBody Music music) {
    return update(music);
  }
}
