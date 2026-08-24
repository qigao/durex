package com.github.durex.music.api;

import com.github.durex.music.model.Music;
import com.github.durex.shared.model.RespData;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/v1/music")
public interface MusicApi {

  @GetExchange
  RespData<List<Music>> list(@RequestParam("title") String title);

  @GetExchange("/{id}")
  RespData<Music> get(@PathVariable("id") String musicId);

  @PostExchange
  RespData<Integer> create(@RequestBody Music music);

  @PutExchange
  RespData<Integer> update(@RequestBody Music music);

  @DeleteExchange("/{id}")
  RespData<Integer> delete(@PathVariable("id") String musicId);
}
