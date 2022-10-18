package com.github.durex.messaging.generator.remote;

import static com.github.durex.messaging.api.enums.RemoteServiceEnum.REACTIVE;

import com.github.durex.messaging.generator.model.MethodInfo;
import com.github.durex.messaging.processor.Helper;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class ReactiveApiCodeGen implements ApiCodeGenStrategy {
  private List<MethodInfo> methodInfos;
  VelocityEngine velocityEngine;

  @Override
  public void filter(List<MethodInfo> methodInfoList) {
    methodInfos =
        methodInfoList.stream()
            .filter(m -> m.getServiceType().equals(REACTIVE))
            .collect(Collectors.toList());
  }

  @Override
  @SneakyThrows
  public void generate(String packageName, String className, Filer filer) {
    var destClassName = packageName + ".Reactive" + Helper.getSimpleName(className);
    final Writer writer = filer.createSourceFile(destClassName).openWriter();
    var template = velocityEngine.getTemplate("templates/ReactiveRemoteServiceApi.vm");
    var context = new VelocityContext();
    context.put("packageName", packageName);
    context.put("className", Helper.getSimpleName(className));
    context.put("methodInfos", methodInfos);
    template.merge(context, writer);
    writer.close();
  }

  public ReactiveApiCodeGen() {
    velocityEngine = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loaders", "class");
    p.setProperty(
        "resource.loader.class.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init(p);
  }
}
