package com.github.durex.messaging.api.model;

public enum CodecEnum {
  BINARY("binary"),
  JSON("json"),
  KRYO("kryo"),
  AVRO("avro"),
  SMILE("smile"),
  CBOR("cbor"),
  MSGPACK("msgpack"),
  ION("ion"),
  JAVA("java"),
  LZ4("lz4"),
  SNAPPY("snappy"),
  TYPED_JSON("typed_json"),
  STRING("string"),
  LONG("long"),
  BYTE_ARRAY("byte_array"),
  COMPOSITE("composite");
  private final String codec;

  CodecEnum(String codec) {
    this.codec = codec;
  }

  public String getCodec() {
    return codec;
  }
}
