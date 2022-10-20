package com.github.durex.messaging.generator.remote;

import static com.github.durex.messaging.api.enums.RemoteServiceEnum.ASYNC;

import com.github.durex.messaging.generator.CodeGenHelper;
import com.github.durex.messaging.generator.model.MethodInfo;
import com.github.durex.messaging.processor.ElementHelper;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class AsyncApiCodeGen implements ApiCodeGenStrategy {
  VelocityEngine velocityEngine;
  private List<MethodInfo> methodInfos;

  public AsyncApiCodeGen() {
    velocityEngine = CodeGenHelper.initVelocity();
  }

  @Override
  public void filter(List<MethodInfo> methodInfoList) {
    methodInfos =
        methodInfoList.stream()
            .filter(m -> m.getServiceType().equals(ASYNC))
            .collect(Collectors.toList());
  }

  @Override
  @SneakyThrows
  public void generate(String packageName, String className, Filer filer) {
    var destClassName = packageName + ".Async" + ElementHelper.getSimpleName(className);
    final Writer writer = filer.createSourceFile(destClassName).openWriter();
    var template = velocityEngine.getTemplate("templates/service/AsyncRemoteServiceApi.vm");
    var context = new VelocityContext();
    context.put("packageName", packageName);
    context.put("className", ElementHelper.getSimpleName(className));
    context.put("methodInfos", methodInfos);
    template.merge(context, writer);
    writer.close();
  }
}
