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
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "cover")
  private String cover;

  @Column(name = "total")
  private int total;

  @Column(name = "editor")
  private String editor;

  @Column(name = "duration")
  private int duration;

  @Column(name = "create_time")
  private LocalDateTime createTime;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      name = "playlist_music",
      joinColumns = {@JoinColumn(name = "playlistId")},
      inverseJoinColumns = {@JoinColumn(name = "musicId")})
  @ToString.Exclude
  private List<Music> musics;
}
