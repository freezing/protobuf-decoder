package io.protobuf;

public class ProtobufFieldList {
  private int size;
  private ProtobufField[] fields;

  public ProtobufFieldList(int initialCapacity) {
    this.fields = new ProtobufField[initialCapacity];
    this.size = 0;
  }

  public int size() {
    return size;
  }

  public void add(ProtobufField field) {
    if (size == fields.length) {
      grow();
    }
    fields[size++] = field;
  }

  public ProtobufField[] array() {
    return fields;
  }

  private void grow() {
    int newCapacity = fields.length * 2;
    ProtobufField[] newFields = new ProtobufField[newCapacity];
    for (int i = 0; i < fields.length; i++) {
      newFields[i] = fields[i];
    }
    fields = newFields;
  }
}
