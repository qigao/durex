package com.github.durex.shared.spring.interceptor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DurexInterceptorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  NullCheckerAspect nullCheckerAspect() {
    return new NullCheckerAspect();
  }

  @Bean
  @ConditionalOnMissingBean
  ValueCheckerAspect valueCheckerAspect() {
    return new ValueCheckerAspect();
  }
}
