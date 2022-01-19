package com.github.durex.music.repository;

import com.github.durex.music.entity.PlayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class PlayListRepository {
  @Inject EntityManager em;

  public static final String EDITOR = "editor";
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String DEVICE = "device";
  public static final String CREATE_TIME = "createTime";

  public PlayList findPlayList(@NotNull String id) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    return em.createQuery(queryBuilder.select(playlist).where(playlist.get(ID).in(id)))
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("playlist not found"));
  }

  @Transactional
  public void createPlayList(PlayList playList) {
    em.persist(playList);
  }

  @Transactional
  public int deletePlayListById(@NotNull String id) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createCriteriaDelete(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    return em.createQuery(queryBuilder.where(playlist.get(ID).in(id))).executeUpdate();
  }

  @Transactional
  public int deletePlayListByIdAndEditor(@NotNull String id, String editor) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createCriteriaDelete(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    var editorPredicate = builder.equal(playlist.get(EDITOR), editor);
    var editorNullPredicate = builder.isNull(playlist.get(EDITOR));
    return em.createQuery(
            queryBuilder.where(
                playlist.get(ID).in(id),
                (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate))
        .executeUpdate();
  }

  @Transactional
  public void updatePlayList(PlayList playList) {
    em.merge(playList);
    em.flush();
  }

  public List<PlayList> findPlayListByIdAndEditor(@NotNull String id, String editor) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    var editorPredicate = builder.equal(playlist.get(EDITOR), editor);
    var editorNullPredicate = builder.isNull(playlist.get(EDITOR));

    queryBuilder
        .select(playlist)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            builder.equal(playlist.get(ID), id))
        .orderBy(builder.desc(playlist.get(CREATE_TIME)));
    return em.createQuery(queryBuilder).getResultList();
  }

  public List<PlayList> findPlayListByEditorAndTitleWithPage(
      String title, String editor, int start, int size) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    var editorPredicate = builder.equal(playlist.get(EDITOR), editor);
    var editorNullPredicate = builder.isNull(playlist.get(EDITOR));
    var titlePredicate = builder.like(playlist.get(TITLE), "%" + title + "%");
    var titleNullPredicate = builder.isNull(playlist.get(TITLE));
    queryBuilder
        .select(playlist)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            (title == null || title.isEmpty()) ? titleNullPredicate : titlePredicate)
        .orderBy(builder.desc(playlist.get(CREATE_TIME)));
    return em.createQuery(queryBuilder)
        .setFirstResult(start * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<PlayList> findPlayListByEditorAndDeviceWithPage(
      String editor, String device, int start, int size) {
    var builder = em.getCriteriaBuilder();
    var queryBuilder = builder.createQuery(PlayList.class);
    var playlist = queryBuilder.from(PlayList.class);
    var editorPredicate = builder.equal(playlist.get(EDITOR), editor);
    var editorNullPredicate = builder.isNull(playlist.get(EDITOR));
    var deviceNullPredicate = builder.isNull(playlist.get(DEVICE));
    var devicePredicate = builder.equal(playlist.get(DEVICE), device);
    queryBuilder
        .select(playlist)
        .where(
            (editor == null || editor.isEmpty()) ? editorNullPredicate : editorPredicate,
            (device == null || device.isEmpty()) ? deviceNullPredicate : devicePredicate)
        .orderBy(builder.desc(playlist.get(CREATE_TIME)));
    return em.createQuery(queryBuilder)
        .setFirstResult(start * size)
        .setMaxResults(size)
        .getResultList();
  }
}
