package com.github.durex.music.repository;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PlayListMusicRepository {
  @Inject MySQLPool client;

  public boolean musicIdInPlayList(String playListId, String musicId) {
    String sql =
        "select exists(select 1 from playlist_music where playlistId=? and musicId=? LIMIT 1)";
    return client
        .preparedQuery(sql)
        .execute(Tuple.of(playListId, musicId))
        .onItem()
        .transform(res -> res.iterator().next().getLong(0) == 1)
        .await()
        .indefinitely();
  }

  public Long musicCountsOfPlayList(String playListId) {
    String sql = "select count(*) from playlist_music where playlistId=? ";
    return client
        .preparedQuery(sql)
        .execute(Tuple.of(playListId))
        .onItem()
        .transform(res -> res.iterator().next().getLong(0))
        .await()
        .indefinitely();
  }

  public List<String> getMusicIdsByPlayListId(String playListId) {
    String sql = "select musicId from playlist_music where playlistId=? order by musicOrder";
    var result =
        client
            .preparedQuery(sql)
            .execute(Tuple.of(playListId))
            .onItem()
            .transformToMulti(Multi.createFrom()::iterable)
            .onItem()
            .transform(res -> res.getString("musicId"));
    return result.collect().asList().await().indefinitely();
  }

  public Long checkMusicAlreadyExistsInPlayList(String playListId, List<String> musicIds) {
    String sql =
        "select count(*) from playlist_music where playlistId=? and musicId in ("
            + musicIds.stream()
                .distinct()
                .map(m -> String.format("'%s'", m))
                .collect(Collectors.joining(","))
            + ")";
    return client
        .preparedQuery(sql)
        .execute(Tuple.of(playListId))
        .onItem()
        .transform(res -> res.iterator().next().getLong(0))
        .await()
        .indefinitely();
  }

  public Long checkIfMusicNotExists(List<String> musicIds) {
    String sql =
        "select count(*) from music where id in ("
            + musicIds.stream()
                .distinct()
                .map(m -> String.format("'%s'", m))
                .collect(Collectors.joining(","))
            + ")";
    return client
        .preparedQuery(sql)
        .execute()
        .onItem()
        .transform(res -> res.iterator().next().getLong(0))
        .await()
        .indefinitely();
  }

  public int addMusicToPlayList(String playListId, String musicId, int order) {
    String sql = "insert into playlist_music(playlistId,musicId,musicOrder) values(?,?,?)";
    return client
        .preparedQuery(sql)
        .execute(Tuple.of(playListId, musicId, order))
        .onItem()
        .transform(SqlResult::rowCount)
        .await()
        .indefinitely();
  }

  public int addMusicToPlayListInBatch(String playListId, List<String> musicIds) {
    String sql = "insert into playlist_music(playlistId,musicId,musicOrder) values(?,?,?)";
    return client
        .preparedQuery(sql)
        .executeBatch(
            IntStream.range(0, musicIds.size())
                .mapToObj(id -> Tuple.of(playListId, musicIds.get(id), id))
                .collect(Collectors.toList()))
        .onItem()
        .transform(
            res -> {
              int total = 0;
              do {
                total += res.rowCount();
              } while ((res = res.next()) != null);
              return total;
            })
        .await()
        .indefinitely();
  }

  public void deleteMusicFromPlayList(String playListId, String musicId) {
    String sql = "delete from playlist_music where playlistId=? and musicId=?";
    client.preparedQuery(sql).execute(Tuple.of(playListId, musicId)).await().indefinitely();
  }

  public void emptyPlayList(String playListId) {
    String sql = "delete from playlist_music where playlistId=? ";
    client.preparedQuery(sql).execute(Tuple.of(playListId)).await().indefinitely();
  }

  public int deleteMusicFromPlayListInBatch(String playListId, List<String> musicIds) {
    String sql = "delete from playlist_music where playlistId=? and musicId=?";
    var rowSet =
        client
            .preparedQuery(sql)
            .executeBatch(
                musicIds.stream()
                    .map(musicId -> Tuple.of(playListId, musicId))
                    .collect(Collectors.toList()));
    return rowSet
        .onItem()
        .transform(
            res -> {
              int total = 0;
              do {
                total += res.rowCount();
              } while ((res = res.next()) != null);
              return total;
            })
        .await()
        .indefinitely();
  }
}
