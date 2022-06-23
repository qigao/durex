package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.InComing;
import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.generator.RedisCodeGenerator;
import com.github.durex.messaging.generator.model.Annotations;
import com.github.durex.messaging.generator.model.PlainAnnotations;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import lombok.SneakyThrows;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.durex.messaging.api.annotation.InComing")
public class InComingProcessor extends AbstractProcessor {

  private Messager messager;
  private Filer javaFile;
  private Types typeUtils;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    javaFile = processingEnv.getFiler();
    typeUtils = processingEnv.getTypeUtils();
  }

  /**
   * this method will process annotation InComing and Topic, then generate code both about
   * MessageListener and register services
   *
   * @param annotations scanned elements container with annotations
   * @param roundEnv annotation processor interface
   * @return always return true
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(InComing.class).stream()
        .filter(ExecutableElement.class::isInstance)
        .forEach(
            element -> {
              var classInfo = Helper.getClassInfo(element);
              var executableElement = (ExecutableElement) element;
              var inComing = executableElement.getAnnotation(InComing.class);
              var inComingTopic = getTopic(inComing);
              var topic = typeUtils.asElement(inComingTopic).getAnnotation(Topic.class);
              var methodInfo = Helper.getMethodInfo(executableElement);
              var templateFields = Helper.getTemplateFields(classInfo, methodInfo);
              Helper.printMessage(classInfo, inComingTopic.toString(), methodInfo, messager);
              var methodName = templateFields.getMethodName();
              var methodNameUpperCase =
                  methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
              var listenerClassName =
                  templateFields.getClassName() + "." + methodNameUpperCase + "Listener";
              listenerCodeGenerate(listenerClassName, javaFile, templateFields);
              var subscriber = topic.subscriber();
              var plainAnnotations =
                  PlainAnnotations.builder()
                      .value(topic.value())
                      .codec(topic.codec())
                      .subscriber(subscriber.isBlank() ? "NotAssigned" : subscriber)
                      .simpleClassName(templateFields.getSimpleClassName())
                      .simpleParamType(templateFields.getSimpleParamType())
                      .className(templateFields.getClassName())
                      .paramType(templateFields.getParamType())
                      .packageName(templateFields.getPackageName())
                      .methodName(templateFields.getMethodName())
                      .build();
              var lifecycleClassName =
                  templateFields.getClassName() + "." + methodNameUpperCase + "Config";
              lifecycleCodeGenerator(lifecycleClassName, javaFile, plainAnnotations);
            });
    return true;
  }

  /**
   * Get the topic from the annotation. this will get the result from exception.
   *
   * @param inComing annotation class
   * @return Topic name
   */
  private TypeMirror getTopic(InComing inComing) {
    try {
      var ignored = inComing.topic();
      throw new IllegalStateException("Expected a MirroredTypeException" + ignored);
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
  }

  /**
   * Generate a java file with the given package name and class name. {@link
   * RedisCodeGenerator#listenerCodeGenerate}, a Listener postfix will be generated.
   *
   * @param listenerClassName class name of the generated subscriber
   * @param filer filer to write the generated java file
   */
  @SneakyThrows
  private void listenerCodeGenerate(
      String listenerClassName, Filer filer, Annotations annotations) {
    final Writer writer = filer.createSourceFile(listenerClassName).openWriter();
    RedisCodeGenerator redisCodeGenerator = new RedisCodeGenerator();
    redisCodeGenerator.listenerCodeGenerate(annotations, writer);
    writer.close();
  }

  /**
   * Generate a java file with the given package name and class name. {@link
   * RedisCodeGenerator#lifecycleCodeGenerator}
   *
   * @param lifecycleClassName class name of the generated subscriber
   * @param filer filer to write the generated java file
   */
  @SneakyThrows
  private void lifecycleCodeGenerator(
      String lifecycleClassName, Filer filer, PlainAnnotations annotationsMap) {
    final Writer writer = filer.createSourceFile(lifecycleClassName).openWriter();
    RedisCodeGenerator redisCodeGenerator = new RedisCodeGenerator();
    redisCodeGenerator.lifecycleCodeGenerator(annotationsMap, writer);
    writer.close();
  }
}
