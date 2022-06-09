package com.github.durex.shared.mapper;

import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public class JacksonObjectMapper implements ObjectMapperCustomizer {
  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    objectMapper.registerModule(new JavaTimeModule());
  }
  @Override
  public int priority() {
    return MINIMUM_PRIORITY; //this is needed in order to ensure that your SimpleModule's Serializer will be applied last and thus override the one coming from the `JavaTimeModule`
  }
}
