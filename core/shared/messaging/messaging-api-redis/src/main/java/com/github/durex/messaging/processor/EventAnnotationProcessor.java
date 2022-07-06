package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.InComing;
import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.generator.RedisCodeGenerator;
import com.github.durex.messaging.generator.model.CodeNameInfo;
import com.github.durex.messaging.generator.model.TopicInfo;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
public class EventAnnotationProcessor extends AbstractProcessor {

  private Filer javaFile;
  private Types typeUtils;
  private RedisCodeGenerator redisCodeGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    javaFile = processingEnv.getFiler();
    typeUtils = processingEnv.getTypeUtils();
    redisCodeGenerator = new RedisCodeGenerator();
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
    roundEnv.getElementsAnnotatedWith(InComing.class).parallelStream()
        .filter(ExecutableElement.class::isInstance)
        .forEach(
            element -> {
              var classInfo = Helper.getClassInfo(element);
              var executableElement = (ExecutableElement) element;
              var methodInfo = Helper.getMethodInfo(executableElement);
              var codeNameInfo = Helper.getCodeNameInfo(classInfo, methodInfo);
              var simpleTargetClassName =
                  Helper.getQualifiedSimpleName(codeNameInfo.getMethodName());
              var targetClassName = codeNameInfo.getClassName() + "." + simpleTargetClassName;
              var topicInfo = getTopicAnnotations(executableElement, codeNameInfo);
              if ("NoGroup".equals(topicInfo.getGroup())) {
                var listenerClassName = targetClassName + "Listener";
                listenerCodeGenerator(listenerClassName, javaFile, codeNameInfo);
                var lifecycleClassName = targetClassName + "Config";
                lifecycleCodeGenerator(lifecycleClassName, javaFile, topicInfo);
              } else {
                var handlerClassName = targetClassName + "Handler";
                handlerCodeGenerate(handlerClassName, javaFile, topicInfo);
                var taskClassName = targetClassName + "Task";
                taskCodeGenerate(taskClassName, javaFile, topicInfo);
                var executorClassName = targetClassName + "Executor";
                executorCodeGenerate(executorClassName, javaFile, topicInfo);
              }
            });
    return true;
  }

  private TopicInfo getTopicAnnotations(
      ExecutableElement executableElement, CodeNameInfo codeNameInfo) {
    var inComingTopic = getInComingTopic(executableElement.getAnnotation(InComing.class));
    var topic = typeUtils.asElement(inComingTopic).getAnnotation(Topic.class);
    var subscriber = topic.subscriber();
    var group = topic.group();
    return TopicInfo.builder()
        .value(topic.value())
        .codec(topic.codec())
        .group(group.isEmpty() ? "NoGroup" : group)
        .subscriber(subscriber.isBlank() ? "NotAssigned" : subscriber)
        .codeNameInfo(codeNameInfo)
        .build();
  }

  /**
   * Get the topic from the annotation. this will get the result from exception.
   *
   * @param inComing annotation class
   * @return The type of the topic that is used in the incoming class
   */
  private TypeMirror getInComingTopic(InComing inComing) {
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
  private void listenerCodeGenerator(
      String listenerClassName, Filer filer, CodeNameInfo codeNameInfo) {
    final Writer writer = filer.createSourceFile(listenerClassName).openWriter();
    redisCodeGenerator.listenerCodeGenerate(codeNameInfo, writer);
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
  private void lifecycleCodeGenerator(String lifecycleClassName, Filer filer, TopicInfo topicInfo) {
    final Writer writer = filer.createSourceFile(lifecycleClassName).openWriter();
    redisCodeGenerator.lifecycleCodeGenerator(topicInfo, writer);
    writer.close();
  }

  /**
   * Generate a java file with the given package name and class name. {@link
   * RedisCodeGenerator#executorCodeGenerator}, a class with Executor postfix will be generated
   *
   * @param executorClassName class name of the ExecutorClass
   * @param filer filer to write the generated java file
   * @param topicInfo info from EventTopic
   */
  @SneakyThrows
  private void executorCodeGenerate(String executorClassName, Filer filer, TopicInfo topicInfo) {
    final Writer writer = filer.createSourceFile(executorClassName).openWriter();
    redisCodeGenerator.executorCodeGenerator(topicInfo, writer);
    writer.close();
  }
  /**
   * Generate a java file with the given package name and class name. {@link
   * RedisCodeGenerator#taskCodeGenerator}, a class with Task postfix will be generated
   *
   * @param taskClassName class name of the TaskClass
   * @param filer filer to write the generated java file
   * @param topicInfo info from EventTopic
   */
  @SneakyThrows
  private void taskCodeGenerate(String taskClassName, Filer filer, TopicInfo topicInfo) {
    final Writer writer = filer.createSourceFile(taskClassName).openWriter();
    redisCodeGenerator.taskCodeGenerator(topicInfo, writer);
    writer.close();
  }
  /**
   * Generate a java file with the given package name and class name. {@link
   * RedisCodeGenerator#handlerCodeGenerator}, a class with Handler postfix will be generated
   *
   * @param handlerClassName class name of the Handler Class
   * @param filer filer to write the generated java file
   * @param topicInfo info from EventTopic
   */
  @SneakyThrows
  private void handlerCodeGenerate(String handlerClassName, Filer filer, TopicInfo topicInfo) {
    final Writer writer = filer.createSourceFile(handlerClassName).openWriter();
    redisCodeGenerator.handlerCodeGenerator(topicInfo, writer);
    writer.close();
  }
}
