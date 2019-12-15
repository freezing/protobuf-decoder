package io.protobuf;

public class DoubleDecoder {
  public static double decodeDouble(byte[] buffer, int offset) {
    return Double.longBitsToDouble(decodeRawLittleEndian64(buffer, offset));
  }

  public static long decodeRawLittleEndian64(byte[] buffer, int offset) {
    return (((buffer[offset] & 0xffL))
        | ((buffer[offset + 1] & 0xffL) << 8)
        | ((buffer[offset + 2] & 0xffL) << 16)
        | ((buffer[offset + 3] & 0xffL) << 24)
        | ((buffer[offset + 4] & 0xffL) << 32)
        | ((buffer[offset + 5] & 0xffL) << 40)
        | ((buffer[offset + 6] & 0xffL) << 48)
        | ((buffer[offset + 7] & 0xffL) << 56));
  }
}
