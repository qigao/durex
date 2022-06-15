package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.Topic;
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

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.durex.messaging.api.annotation.Topic")
public class TopicProcessor extends AbstractProcessor {

  Messager messager;
  Filer javaFile;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    javaFile = processingEnv.getFiler();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(Topic.class).stream()
        .filter(TypeElement.class::isInstance)
        .forEach(
            element -> {
              var result = element.getAnnotation(Topic.class).value();
              messager.printMessage(
                  Diagnostic.Kind.NOTE,
                  "classInfo: " + element.getKind() + " " + element.getSimpleName() + " " + result);
            });
    return true;
  }
}
