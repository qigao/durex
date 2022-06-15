package com.github.durex.messaging.redis;

import com.github.durex.messaging.api.model.CodecEnum;
import lombok.experimental.UtilityClass;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.Kryo5Codec;
import org.redisson.codec.LZ4Codec;
import org.redisson.codec.MarshallingCodec;
import org.redisson.codec.MsgPackJacksonCodec;

@UtilityClass
public class RedisCodec {
  public Codec getCodec(CodecEnum codecEnum) {
    switch (codecEnum) {
      case JSON:
        return new JsonJacksonCodec();
      case STRING:
        return new StringCodec();
      case BYTE_ARRAY:
        return new ByteArrayCodec();
      case BINARY:
        return new MarshallingCodec();
      case KRYO:
        return new Kryo5Codec();
      case MSGPACK:
        return new MsgPackJacksonCodec();
      case LZ4:
        return new LZ4Codec();
      default:
        throw new IllegalArgumentException("Unknown codec enum: " + codecEnum);
    }
  }
}
