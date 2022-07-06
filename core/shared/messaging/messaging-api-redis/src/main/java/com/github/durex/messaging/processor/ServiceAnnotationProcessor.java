package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.RemoteService;
import com.github.durex.messaging.generator.RedisCodeGenerator;
import com.github.durex.messaging.generator.model.ServiceInfo;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import lombok.SneakyThrows;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.durex.messaging.api.annotation.RemoteService")
public class ServiceAnnotationProcessor extends AbstractProcessor {
  private Filer javaFile;
  private Messager messager;
  private RedisCodeGenerator redisCodeGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    javaFile = processingEnv.getFiler();
    messager = processingEnv.getMessager();
    redisCodeGenerator = new RedisCodeGenerator();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(RemoteService.class).parallelStream()
        .filter(TypeElement.class::isInstance)
        .forEach(
            element -> {
              var interfaceName = ((TypeElement) element).getInterfaces().toString();
              var className = ((TypeElement) element).getQualifiedName().toString();
              var serviceInfo =
                  ServiceInfo.builder()
                      .packageName(Helper.getParentName(className))
                      .className(Helper.getSimpleName(className))
                      .interfaceName(interfaceName)
                      .simpleInterfaceName(Helper.getSimpleName(interfaceName))
                      .build();
              messager.printMessage(
                  Diagnostic.Kind.NOTE,
                  String.format(
                      "package: %s ,interface %s", Helper.getParentName(className), interfaceName));
              var daemonName = className + "Daemon";
              daemonCodeGenerator(daemonName, javaFile, serviceInfo);
              var lifeCycleName = className + "LifeCycle";
              daemonLiceCycleCodeGenerator(lifeCycleName, javaFile, serviceInfo);
            });
    return false;
  }

  @SneakyThrows
  private void daemonCodeGenerator(String daemonClassName, Filer filer, ServiceInfo serviceInfo) {
    final Writer writer = filer.createSourceFile(daemonClassName).openWriter();
    redisCodeGenerator.daemonCodeGenerator(serviceInfo, writer);
    writer.close();
  }

  @SneakyThrows
  private void daemonLiceCycleCodeGenerator(
      String lifeCycleClassName, Filer filer, ServiceInfo serviceInfo) {
    final Writer writer = filer.createSourceFile(lifeCycleClassName).openWriter();
    redisCodeGenerator.lifeCycleCodeGenerator(serviceInfo, writer);
    writer.close();
  }
}
