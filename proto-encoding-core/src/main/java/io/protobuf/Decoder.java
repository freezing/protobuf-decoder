package io.protobuf;

import com.google.common.base.Preconditions;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class Decoder {
  private static final int EXPECTED_NUMBER_OF_FIELDS = 8;

  private final int[] tmpContainer = new int[2];

  /**
   * Decodes the data starting at the specified offset as a ProtobufObject.
   *
   * Precondition:
   *   - data is a valid protobuf encoding.
   *
   * Protobuf message structure is an array of key-value pairs.
   *
   * Each key is a varint-encoded representation of the field ID and wire type.
   *
   * Wire type is describes the type of the value:
   *   - 0 the value is varint
   *   - 1 the value is 64-bit primitive (e.g. double)
   *   - 2 the value is a byte array
   *   - 3 and 4 are deprecated and unsupported
   *   - 5 the value is 32-bit primite (e.g. float)
   *
   * Depending on the wire type, the encoding of the value may or may not have a varint-encoded
   * length. Only wire type 2 comes with varint-encoded length that specifies the length of
   * the value.
   */
  public ProtobufObject decode(byte[] data, int offsetStart, int offsetEnd) {
    Preconditions.checkArgument(offsetEnd - offsetStart > 1);
    ProtobufFieldList fields = new ProtobufFieldList(EXPECTED_NUMBER_OF_FIELDS);

    int fieldOffset = offsetStart;
    while (fieldOffset < offsetEnd) {
      ProtobufField protobufField = decodeField(data, fieldOffset, tmpContainer);
      fields.add(protobufField);
      fieldOffset = protobufField.valueEndOffset();
    }
    return new ProtobufObject(data, offsetStart, offsetEnd, fields);
  }

  private static ProtobufField decodeField(byte[] data, int fieldOffset, int[] tmpResultContainer) {
    int afterKeyOffset = getVarInt(data, fieldOffset, tmpResultContainer);
    int key = tmpResultContainer[0];
    valueOffsetForWireType(FieldKey.wireType(key), data, afterKeyOffset, tmpResultContainer);
    int valueStartOffset = tmpResultContainer[0];
    int valueEndOffset = tmpResultContainer[1];

    return new ProtobufField(key, valueStartOffset, valueEndOffset);
  }

  /**
   * Returns the value start offset and value end offset for the specified wire type into the output
   * result[0] and result[1] respectively.
   */
  private static void valueOffsetForWireType(int wireType, byte[] data, int offset, int[] result) {
    switch (wireType) {
      case 0:
        int afterReadOffset = getVarIntOffset(data, offset);
        result[0] = offset;
        result[1] = afterReadOffset;
        break;
      case 1:
        result[0] = offset;
        // 8 bytes for 64-bit values.
        result[1] = offset + 8;
        break;
      case 2:
        // byte arrays come with length as varint.
        int valueStartOffset = getVarInt(data, offset, result);
        // Result[0] now contains the length of the value.
        result[1] = valueStartOffset + result[0];
        result[0] = valueStartOffset;
        break;
      case 5:
        result[0] = offset;
        // 4 bytes for 32-but values.
        result[1] = offset + 4;
        break;
      default:
        throw new RuntimeException("Unknown wire type: " + wireType);
    }
  }

  /**
   * Reads a varint  from src, places its values into the first element of
   * dst and returns the offset in to src of the first byte after the varint.
   *
   * @param src source buffer to retrieve from
   * @param offset offset within src
   * @param dst the resulting int value
   * @return the updated offset after reading the varint
   */
  private static int getVarInt(byte[] src, int offset, int[] dst) {
    int result = 0;
    int shift = 0;
    int b;
    do {
      if (shift >= 32) {
        // Out of range
        throw new IndexOutOfBoundsException("varint too long");
      }
      // Get 7 bits from next byte
      b = src[offset++];
      result |= (b & 0x7F) << shift;
      shift += 7;
    } while ((b & 0x80) != 0);
    dst[0] = result;
    return offset;
  }

  private static int getVarIntOffset(byte[] src, int offset) {
    int b;
    do {
      b = src[offset++];
    } while ((b & 0x80) != 0);
    return offset;
  }
}
