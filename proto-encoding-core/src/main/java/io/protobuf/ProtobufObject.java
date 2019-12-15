package io.protobuf;

public class ProtobufObject {
  private final byte[] data;
  private final int startOffset;
  private final int endOffset;
  private final ProtobufFieldList fields;

  public ProtobufObject(byte[] data, int startOffset, int endOffset, ProtobufFieldList fields) {
    this.data = data;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.fields = fields;
  }

  public byte[] data() {
    return data;
  }

  public ProtobufFieldList fields() {
    return fields;
  }

  public int endOffset() {
    return endOffset;
  }

  public int startOffset() {
    return startOffset;
  }
}
