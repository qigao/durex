package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.QuarkusDaemon;
import com.github.durex.messaging.generator.RedisCodeGenerator;
import com.github.durex.messaging.generator.model.ServiceInfo;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import lombok.SneakyThrows;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.durex.messaging.api.annotation.QuarkusDaemon")
public class QuarkusDaemonAnnotation extends AbstractProcessor {
  private Filer javaFile;
  private RedisCodeGenerator redisCodeGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    javaFile = processingEnv.getFiler();
    redisCodeGenerator = new RedisCodeGenerator();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(QuarkusDaemon.class).stream()
        .filter(TypeElement.class::isInstance)
        .forEach(
            element -> {
              TypeElement typeElement = (TypeElement) element;
              var className = typeElement.getQualifiedName().toString();
              var serviceInfo =
                  ServiceInfo.builder()
                      .packageName(Helper.getParentName(className))
                      .className(Helper.getSimpleName(className))
                      .build();
              var daemonName = className + "Daemon";
              daemonCodeGenerator(daemonName, javaFile, serviceInfo);
            });
    return false;
  }

  @SneakyThrows
  private void daemonCodeGenerator(String daemonClassName, Filer filer, ServiceInfo serviceInfo) {
    final Writer writer = filer.createSourceFile(daemonClassName).openWriter();
    redisCodeGenerator.daemonCodeGenerator(serviceInfo, writer);
    writer.close();
  }
}
