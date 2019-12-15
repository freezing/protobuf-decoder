package io.protobuf;

public class FieldKey {
  public static int fieldId(int key) {
    return key >> 3;
  }

  public static int wireType(int key) {
    return key & 0b111;
  }
}
