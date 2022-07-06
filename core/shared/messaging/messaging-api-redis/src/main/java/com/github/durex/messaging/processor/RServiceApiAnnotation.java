package com.github.durex.messaging.processor;

import com.github.durex.messaging.api.annotation.RemoteServiceApi;
import com.github.durex.messaging.generator.RedisCodeGenerator;
import com.github.durex.messaging.generator.model.MethodInfo;
import com.github.durex.messaging.generator.remote.ApiCodeGenStrategy;
import com.github.durex.messaging.generator.remote.AsyncApiCodeGen;
import com.github.durex.messaging.generator.remote.ReactiveApiCodeGen;
import com.github.durex.messaging.generator.remote.RxJavaApiCodeGen;
import com.github.durex.messaging.generator.remote.SyncApiCodeGen;
import com.github.durex.utils.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.github.durex.messaging.api.annotation.RemoteServiceApi")
public class RServiceApiAnnotation extends AbstractProcessor {
  private Filer javaFile;
  private RedisCodeGenerator redisCodeGenerator;
  private Messager messager;
  private Elements elementUtils;
  private Types typeUtils;

  private List<MethodInfo> methodInfoList;

  @SneakyThrows
  private static void generateCode(
      Class<? extends ApiCodeGenStrategy> clazz,
      Pair<String, String> classInfo,
      Filer filer,
      List<MethodInfo> methodInfoList) {
    var strategy = clazz.getDeclaredConstructor().newInstance();
    strategy.filter(methodInfoList);
    strategy.generate(classInfo.getFirst(), classInfo.getSecond(), filer);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    javaFile = processingEnv.getFiler();
    messager = processingEnv.getMessager();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    redisCodeGenerator = new RedisCodeGenerator();
    methodInfoList = new ArrayList<>();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<Pair<String, String>> apiInfo = new HashSet<>();
    roundEnv.getElementsAnnotatedWith(RemoteServiceApi.class).stream()
        .filter(ExecutableElement.class::isInstance)
        .forEach(
            element -> {
              var classInfo = Helper.getClassInfo(element);
              var serviceType = element.getAnnotation(RemoteServiceApi.class).serviceType();
              var executableElement = (ExecutableElement) element;
              var methodInfo = Helper.extractMethodInfo(executableElement);
              methodInfo.setServiceType(serviceType);
              methodInfoList.add(methodInfo);
              apiInfo.add(classInfo);
            });
    if (apiInfo.isEmpty()) return true;
    var api = apiInfo.stream().findFirst().orElseGet(() -> Pair.of("", ""));
    generateCode(SyncApiCodeGen.class, api, javaFile, methodInfoList);
    generateCode(AsyncApiCodeGen.class, api, javaFile, methodInfoList);
    generateCode(RxJavaApiCodeGen.class, api, javaFile, methodInfoList);
    generateCode(ReactiveApiCodeGen.class, api, javaFile, methodInfoList);
    return true;
  }
}
