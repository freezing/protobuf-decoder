package io.protobuf;

public class ProtobufField {
  private final int key;
  private final int valueStartOffset;
  private final int valueEndOffset;

  public ProtobufField(int key, int valueStartOffset, int valueEndOffset) {
    this.key = key;
    this.valueStartOffset = valueStartOffset;
    this.valueEndOffset = valueEndOffset;
  }

  public int key() {
    return key;
  }

  public int valueStartOffset() {
    return valueStartOffset;
  }

  public int valueEndOffset() {
    return valueEndOffset;
  }
}
