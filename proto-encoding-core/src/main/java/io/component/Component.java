package io.component;

import io.protobuf.FieldKey;
import io.protobuf.ProtobufField;
import io.protobuf.ProtobufObject;
import java.nio.ByteBuffer;
import org.agrona.collections.Int2ObjectHashMap;

public class Component {
  private final Int2ObjectHashMap<ByteBuffer> fields;

  public Component(ProtobufObject obj) {
    this.fields = new Int2ObjectHashMap<>(obj.fields().size(), 0.75f);
    for (int i = 0; i < obj.fields().size(); i++) {
      ProtobufField field = obj.fields().array()[i];
      ByteBuffer byteBuffer = ByteBuffer.allocate(field.valueEndOffset() - field.valueStartOffset());
      byteBuffer.put(obj.data(), field.valueStartOffset(), field.valueEndOffset() - field.valueStartOffset());
      fields.put(FieldKey.fieldId(field.key()), byteBuffer);
    }
  }

  // Doesn't work with proto2 collections because they are not packed by default.
  // TODO: Need to understand how does merge algorithm without schema work (if it's possible).
  public void merge(ProtobufObject obj) {
    for (int i = 0; i < obj.fields().size(); i++) {
      ProtobufField field = obj.fields().array()[i];
      int fieldId = FieldKey.fieldId(field.key());
      int valueLength = field.valueEndOffset() - field.valueStartOffset();

      ByteBuffer fieldBuffer = fields.get(fieldId);
      if (fieldBuffer != null && fieldBuffer.capacity() <= valueLength) {
        fieldBuffer.clear();
      } else {
        fieldBuffer = ByteBuffer.allocate(valueLength);
      }
      fieldBuffer.put(obj.data(), field.valueStartOffset(), valueLength);
      fields.put(fieldId, fieldBuffer);
    }
  }
}
