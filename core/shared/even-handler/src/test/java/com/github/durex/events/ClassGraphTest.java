package com.github.durex.events;

import com.github.durex.events.api.EventHandler;
import io.github.classgraph.ClassGraph;
import java.util.List;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
@DisplayName("classgraph library tests")
class ClassGraphTest {

  @Test
  @DisplayName("should get classes recursively in given package")
  void test() {
    @Cleanup
    val scanResult =
        new ClassGraph().acceptPackages(getClass().getPackage().getName()).enableAllInfo().scan();
    List<Class<?>> classes =
        scanResult.getClassesWithMethodAnnotation(EventHandler.class.getName()).loadClasses();
    Assertions.assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
