package com.github.durex.music.util.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.music.model.MusicRequest;
import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.model.PlayListRequest;
import com.github.durex.utils.Json;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Data {
  public static final String PLAYLIST_WITH_3_MUSICS_JSON =
      "src/test/resources/json/playlist/playlist_with_3_musics.json";
  public static final String SINGLE_MUSIC_JSON =
      "src/test/resources/json/music/single_music_id_6.json";
  public static final String FIVE_MUSICS_JSON = "src/test/resources/json/music/five_musics.json";
  public static final String CHINESE_MUSICS_JSON = "src/test/resources/json/music/chinese.json";

  public static final String PLAYLIST_WITH_5_MUSICS_JSON =
      "src/test/resources/json/playlist/playlist_with_5_musics.json";

  public static PlayListRequest givenPlayListWithInitialMusics() throws IOException {
    return givenPlayListFromFile(PLAYLIST_WITH_3_MUSICS_JSON);
  }

  public static PlayListRequest givenPlayListWithFiveMusics() throws IOException {
    return givenPlayListFromFile(PLAYLIST_WITH_5_MUSICS_JSON);
  }

  private static PlayListRequest givenPlayListFromFile(String jsonFilePath) throws IOException {
    return Json.read(Path.of(jsonFilePath).toFile(), PlayListRequest.class);
  }

  public static MusicRequest givenAMusic() throws IOException {
    return Json.read(Path.of(SINGLE_MUSIC_JSON).toFile(), MusicRequest.class);
  }

  public static List<MusicRequest> givenFiveMusics() throws IOException {
    return Json.read(Path.of(FIVE_MUSICS_JSON).toFile(), new TypeReference<>() {});
  }

  public static List<MusicRequest> givenMusicsWithChineseName() throws IOException {
    return Json.read(Path.of(CHINESE_MUSICS_JSON).toFile(), new TypeReference<>() {});
  }

  public static List<OrderedMusicRequest> givenMusicsWithOrder() {
    String MUSIC_ID_6 = "wtVNDNP3YfqCOH7wKXStcEc61U6";
    String MUSIC_ID_7 = "wtVNDNP3YfqCOH7wKXStcEc61U7";
    String MUSIC_ID_8 = "wtVNDNP3YfqCOH7wKXStcEc61U8";
    String MUSIC_ID_9 = "wtVNDNP3YfqCOH7wKXStcEc61U9";
    var musicAWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_6).order(5).build();
    var musicBWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_7).order(1).build();
    var musicCWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_8).order(3).build();
    var musicDWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_9).order(2).build();
    return List.of(musicAWithOrder, musicBWithOrder, musicCWithOrder, musicDWithOrder);
  }
}
