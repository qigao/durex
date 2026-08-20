package com.github.durex.music.spring;

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.support.Helper;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/music")
public class MusicHttpController {
  private final MusicService musicService;

  public MusicHttpController(MusicService musicService) {
    this.musicService = musicService;
  }

  @GetMapping({"", "/"})
  public RespData<List<Music>> getMusic(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "id", required = false) String musicId,
      @RequestParam(value = "offset", defaultValue = "10") int offset) {
    return RespData.of(musicService.getMusicsByTitle(title), Helper.okResponse());
  }

  @GetMapping("/{id}")
  public RespData<Music> getMusic(@PathVariable("id") String musicId) {
    return RespData.of(musicService.getMusicById(musicId), Helper.okResponse());
  }

  @PostMapping({"", "/"})
  public RespData<Integer> createMusic(@RequestBody Music music) {
    return RespData.of(musicService.createMusic(music), Helper.okResponse());
  }

  @PutMapping({"", "/"})
  public RespData<Integer> updateMusic(@RequestBody Music music) {
    return RespData.of(musicService.updateMusic(music), Helper.okResponse());
  }

  @DeleteMapping("/{id}")
  public RespData<Integer> deleteMusic(@PathVariable("id") String musicId) {
    return RespData.of(musicService.deleteMusicById(musicId), Helper.okResponse());
  }
}
