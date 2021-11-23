package com.github.durex.basic.entity;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@Cacheable
@EqualsAndHashCode(of = "id")
@Table(name = "playlist")
public class PlayList {
  @Id
  @Column(name = "id", unique = true, nullable = false, columnDefinition = "VARCHAR(128)")
  private String id;

  @Column(name = "title", columnDefinition = "VARCHAR(255)")
  private String title;

  @Column(name = "description", columnDefinition = "VARCHAR(255)")
  private String description;

  @Column(name = "coverUrl", columnDefinition = "VARCHAR(1024)")
  private String coverUrl;

  @Column(name = "total", columnDefinition = "INT default 0")
  private int total;

  @Column(name = "editor", columnDefinition = "VARCHAR(255)")
  private String editor;

  @Column(name = "duration", columnDefinition = "INT default 0")
  private int duration;

  @Column(name = "create_time", columnDefinition = "DATETIME")
  private LocalDateTime createTime;

  @Column(name = "update_time", columnDefinition = "DATETIME")
  private LocalDateTime updateTime;

  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      name = "playlist_music",
      joinColumns = {@JoinColumn(name = "playlistId")},
      inverseJoinColumns = {@JoinColumn(name = "musicId")})
  @ToString.Exclude
  private List<Music> musics;
}
