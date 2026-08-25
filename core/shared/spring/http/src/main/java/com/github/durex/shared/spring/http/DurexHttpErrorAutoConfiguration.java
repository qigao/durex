package com.github.durex.shared.spring.http;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DurexHttpErrorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(DurexHttpExceptionHandler.class)
  DurexHttpExceptionHandler durexHttpExceptionHandler() {
    return new DurexHttpExceptionHandler();
  }
}
