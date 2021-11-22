package com.github.durex.basic.entity;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "music")
public class Music {
  @Id
  @Column(name = "id", unique = true, nullable = false)
  private String id;

  @Column(name = "url", nullable = false, columnDefinition = "TEXT")
  private String url;

  @Column(name = "voice", nullable = false, columnDefinition = "TEXT")
  private String voice;

  @Column(name = "duration", columnDefinition = "INT")
  private int duration;

  @Column(name = "cover", columnDefinition = "TEXT")
  private String cover;

  @Column(name = "title", nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(name = "artist", columnDefinition = "TEXT")
  private String artist;

  @Column(name = "editor", columnDefinition = "TEXT")
  private String editor;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "create_time", columnDefinition = "DATETIME")
  private LocalDateTime createTime;

  @Column(name = "update_time", columnDefinition = "DATETIME")
  private LocalDateTime updateTime;

  @ManyToMany(mappedBy = "musics")
  @ToString.Exclude
  private List<PlayList> playList;
}
