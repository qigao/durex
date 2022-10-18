package com.github.durex.messaging.generator.model;

import com.github.durex.messaging.api.enums.CodecEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicInfo {
  private CodecEnum codec;
  private String value;
  private String group;
  private String subscriber;
  private CodeNameInfo codeNameInfo;
}
