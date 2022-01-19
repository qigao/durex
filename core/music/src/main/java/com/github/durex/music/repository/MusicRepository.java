package com.github.durex.music.repository;

import com.github.durex.music.entity.Music;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class MusicRepository {
  public static final String EDITOR = "editor";
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String DEVICE = "device";
  @Inject EntityManager em;

  @Transactional
  public void createMusic(Music music) {
    em.persist(music);
    em.flush();
  }

  @Transactional
  public void deleteMusicById(String id) {
    var music = findMusicById(id);
    em.remove(music);
    em.flush();
  }

  @Transactional
  public void updateMusic(Music music) {
    em.merge(music);
    em.flush();
  }

  public Music findMusicById(@NotNull String id) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(Music.class);
    var root = queryBuilder.from(Music.class);
    queryBuilder.select(root).where(builder.equal(root.get(ID), id));
    return em.createQuery(queryBuilder)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Music not found"));
  }

  public Music findMusicsByIdAndEditor(@NotNull String id, String editor) {
    var cb = em.getCriteriaBuilder();
    var queryBuilder = cb.createQuery(Music.class);
    var root = queryBuilder.from(Music.class);
    var editorPredicate = cb.equal(root.get(EDITOR), editor);
    var editorNullPredicate = cb.isNull(root.get(EDITOR));
    var idPredicate = cb.equal(root.get(ID), id);
    queryBuilder
        .select(root)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            idPredicate)
        .orderBy(cb.asc(root.get(ID)));
    return em.createQuery(queryBuilder)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Music not found"));
  }

  public List<Music> findMusicsByEditor(String editor, int start, int size) {
    var cb = em.getCriteriaBuilder();
    var queryBuilder = cb.createQuery(Music.class);
    var root = queryBuilder.from(Music.class);

    var editorNullBuilder = cb.isNull(root.get(EDITOR));

    var regularBuilder = cb.equal(root.get(EDITOR), editor);
    queryBuilder
        .select(root)
        .where((editor == null || editor.isEmpty()) ? editorNullBuilder : regularBuilder)
        .orderBy(cb.asc(root.get(ID)));
    return em.createQuery(queryBuilder)
        .setMaxResults(size)
        .setFirstResult(start * size)
        .getResultList();
  }

  public List<Music> findMusicByEditorAndDeviceWithPage(
      String editor, String device, int start, int size) {
    var cb = em.getCriteriaBuilder();
    var queryBuilder = cb.createQuery(Music.class);
    var root = queryBuilder.from(Music.class);
    var editorPredicate = cb.equal(root.get(EDITOR), editor);
    var editorNullPredicate = cb.isNull(root.get(EDITOR));
    var devicePredicate = cb.equal(root.get(DEVICE), device);
    var deviceNullPredicate = cb.isNull(root.get(DEVICE));
    queryBuilder
        .select(root)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            (device == null || device.isEmpty()) ? deviceNullPredicate : devicePredicate)
        .orderBy(cb.asc(root.get(ID)));
    return em.createQuery(queryBuilder)
        .setMaxResults(size)
        .setFirstResult(start * size)
        .getResultList();
  }

  public List<Music> findMusicsByTitleAndEditorWithPage(
      String title, String editor, int start, int size) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(Music.class);
    var music = queryBuilder.from(Music.class);
    var titlePredicate = builder.like(music.get(TITLE), "%" + title + "%");
    var titleNullPredicate = builder.isNull(music.get(TITLE));
    var editorPredicate = builder.equal(music.get(EDITOR), editor);
    var editorNullPredicate = builder.isNull(music.get(EDITOR));

    queryBuilder
        .select(music)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            (title == null || title.isEmpty()) ? titleNullPredicate : titlePredicate)
        .orderBy(builder.asc(music.get(ID)));
    return em.createQuery(queryBuilder)
        .setMaxResults(size)
        .setFirstResult(start * size)
        .getResultList();
  }

  public List<Music> findMusicsByTitleAndDevice(String title, String device) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(Music.class);
    var music = queryBuilder.from(Music.class);
    var deviceNullPredicate = builder.isNull(music.get(DEVICE));
    var titlePredicate = builder.like(music.get(TITLE), "%" + title + "%");
    var titleNullPredicate = builder.isNull(music.get(TITLE));
    queryBuilder
        .select(music)
        .where(
            (device == null || device.isEmpty())
                ? deviceNullPredicate
                : builder.equal(music.get(DEVICE), device),
            (title == null || title.isEmpty()) ? titleNullPredicate : titlePredicate)
        .orderBy(builder.asc(music.get(ID)));

    return em.createQuery(queryBuilder).getResultList();
  }
}
