package com.github.durex.messaging.processor;

import com.github.durex.messaging.generator.model.CodeNameInfo;
import com.github.durex.messaging.generator.model.MethodInfo;
import com.github.durex.utils.Pair;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Helper {

  public Pair<String, String> getClassInfo(Element element) {
    final Element enclosingElement = element.getEnclosingElement();
    var className = enclosingElement.toString();
    var packageName = enclosingElement.getEnclosingElement().toString();
    return Pair.of(packageName, className);
  }

  public Pair<String, String> getMethodInfo(ExecutableElement executableElement) {
    var methodName = executableElement.getSimpleName().toString();
    var paramType = executableElement.getParameters().get(0).asType().toString();
    return Pair.of(methodName, paramType);
  }

  public CodeNameInfo buildCodeNameInfo(
      Pair<String, String> classInfo, Pair<String, String> methodInfo) {
    final String className = classInfo.getSecond();
    final String packageName = classInfo.getFirst();
    final String methodName = methodInfo.getFirst();
    final String paramType = methodInfo.getSecond();
    return CodeNameInfo.builder()
        .packageName(packageName)
        .className(className)
        .methodName(methodName)
        .paramType(paramType)
        .simpleClassName(className.substring(className.lastIndexOf(".") + 1))
        .simpleParamType(paramType.substring(paramType.lastIndexOf(".") + 1))
        .build();
  }

  public String transformToCamelCase(String simpleName) {
    return simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
  }

  public String getParentName(String fullClassName) {
    return fullClassName.substring(0, fullClassName.lastIndexOf("."));
  }

  public String getSimpleName(String fullClassName) {
    return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
  }

  public void printMessage(
      Pair<String, String> classInfo,
      String inComingTopic,
      Pair<String, String> methodInfo,
      Messager messager) {
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        String.format(
            "package: %s class: %s method: %s paramType: %s topic: %s",
            classInfo.getFirst(),
            classInfo.getSecond(),
            methodInfo.getFirst(),
            methodInfo.getSecond(),
            inComingTopic));
  }

  public void printMessage(String message, Messager messager) {
    messager.printMessage(Diagnostic.Kind.NOTE, message);
  }

  public MethodInfo extractMethodInfo(ExecutableElement executableElement) {
    var parameters = executableElement.getParameters();
    var methodName = executableElement.getSimpleName();
    var returnType = executableElement.getReturnType();
    var params =
        parameters.stream()
            .map(p -> new StringBuffer(p.asType().toString()).append(" ").append(p))
            .collect(Collectors.joining(","));
    return MethodInfo.builder()
        .methodName(methodName)
        .returnType(returnType)
        .params(params)
        // .serviceType(serviceType)
        .build();
  }
}
