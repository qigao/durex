package com.github.durex.messaging.processor;

import com.github.durex.messaging.generator.model.Annotations;
import com.github.durex.utils.Pair;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Helper {

  public Pair<String, String> getClassInfo(Element element) {
    var className = element.getEnclosingElement().toString();
    var packageName = element.getEnclosingElement().getEnclosingElement().toString();
    return Pair.of(packageName, className);
  }

  public Pair<String, String> getMethodInfo(ExecutableElement executableElement) {
    var methodName = executableElement.getSimpleName().toString();
    var paramType = executableElement.getParameters().get(0).asType().toString();
    return Pair.of(methodName, paramType);
  }

  public Annotations getTemplateFields(
      Pair<String, String> classInfo, Pair<String, String> methodInfo) {
    return Annotations.builder()
        .packageName(classInfo.getFirst())
        .className(classInfo.getSecond())
        .methodName(methodInfo.getFirst())
        .paramType(methodInfo.getSecond())
        .simpleClassName(
            classInfo.getSecond().substring(classInfo.getSecond().lastIndexOf(".") + 1))
        .simpleParamType(
            methodInfo.getSecond().substring(methodInfo.getSecond().lastIndexOf(".") + 1))
        .build();
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
}
