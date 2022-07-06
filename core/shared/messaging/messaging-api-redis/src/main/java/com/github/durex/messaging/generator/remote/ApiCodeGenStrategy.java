package com.github.durex.messaging.generator.remote;

import com.github.durex.messaging.generator.model.MethodInfo;
import java.util.List;
import javax.annotation.processing.Filer;

public interface ApiCodeGenStrategy {
  void filter(List<MethodInfo> methodInfoList);

  void generate(String packageName, String className, Filer filer);
}
