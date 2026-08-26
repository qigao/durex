package com.github.durex.music.spring;

import static com.github.durex.shared.exceptions.model.ErrorCode.DELETE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.shared.exceptions.ApiException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class MusicSpringInterceptorTest {

  @Autowired private MusicService musicService;

  @Test
  void emptyCollectionQueryIsAValidBusinessResult() {
    assertTrue(musicService.getMusicsByTitle("missing-title").isEmpty());
  }

  @Test
  void zeroRowMutationKeepsStructuredDeleteError() {
    var exception =
        assertThrows(ApiException.class, () -> musicService.deleteMusicById("missing-id"));

    assertEquals(DELETE_ERROR, exception.getErrorResponse().errorCode());
  }

  @Test
  void musicServiceDoesNotRequireAopProxyForBusinessOutcomeChecks() {
    assertFalse(AopUtils.isAopProxy(musicService));
  }

  @Test
  void businessServicesDoNotDeclareLegacyCheckerAnnotations() {
    boolean hasLegacyChecker =
        Stream.of(MusicService.class, PlaylistService.class)
            .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
            .flatMap(method -> Arrays.stream(method.getDeclaredAnnotations()))
            .map(annotation -> annotation.annotationType().getName())
            .anyMatch(
                name ->
                    name.equals("com.github.durex.shared.annotation.NullChecker")
                        || name.equals("com.github.durex.shared.annotation.ValueChecker"));

    assertFalse(hasLegacyChecker);
  }
}
