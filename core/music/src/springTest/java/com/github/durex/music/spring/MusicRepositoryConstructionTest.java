package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.github.durex.music.repository.MusicRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;

class MusicRepositoryConstructionTest {

  @Test
  void repositoryCanBeConstructedWithoutFrameworkContainer() {
    var dsl = mock(DSLContext.class);

    var repository = new MusicRepository(dsl);

    assertNotNull(repository);
  }
}
