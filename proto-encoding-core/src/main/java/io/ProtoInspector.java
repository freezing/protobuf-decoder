package io;

import com.google.protobuf.CodedInputStream;
import io.protobuf.Decoder;
import io.protobuf.DoubleDecoder;
import io.protobuf.FieldKey;
import io.protobuf.ProtobufField;
import io.protobuf.ProtobufObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.apache.commons.codec.binary.Hex;

public class ProtoInspector {
  private static final Decoder decoder = new Decoder();

  public static void main(String[] args) throws IOException {
    Path path = Paths.get(DebuggingConfig.POSITION_PROTO_DUMP);
    byte[] data = Files.readAllBytes(path);
    ProtobufObject protoObject = decoder.decode(data, 0, data.length);
    prettyPrint(protoObject, /* recursive */ true);
  }

  private static void prettyPrint(@Nullable ProtobufObject protoObject, boolean recursive)
      throws IOException {
    prettyPrint(protoObject, recursive, /* indent */ 0);
  }

  private static void prettyPrint(@Nullable ProtobufObject protoObject, boolean recursive,
      int indent) throws IOException {
    if (protoObject == null) {
      return;
    }

    String indentation = makeIndentation(indent);
    System.out.println(indentation + "ProtoObject has " + protoObject.fields().size() + " fields.");
    System.out.println("ProtoObject full payload: " + String.valueOf(Hex.encodeHex(
        copyBytes(protoObject.data(), protoObject.startOffset(), protoObject.endOffset()), false)));
    for (int i = 0; i < protoObject.fields().size(); i++) {
      ProtobufField field = protoObject.fields().array()[i];
      prettyPrint(field, protoObject.data(), recursive, indent + 2);
    }
  }

  private static void prettyPrint(ProtobufField protobufField, byte[] data, boolean recursive,
      int indent) throws IOException {
    String indentation = makeIndentation(indent);
    int key = protobufField.key();
    System.out
        .println(String
            .format("%sKey{fieldId=%d, wireType=%d}", indentation, FieldKey.fieldId(key),
                FieldKey.wireType(key)));
    System.out.println(String.format("%sStartOffset: %d EndOffset: %d Length: %d", indentation,
        protobufField.valueStartOffset(),
        protobufField.valueEndOffset(),
        protobufField.valueEndOffset() - protobufField.valueStartOffset()));
    byte[] payloadBytes = copyBytes(data, protobufField);
    System.out.println(String
        .format("%sPayload: %s", indentation,
            String.valueOf(Hex.encodeHex(payloadBytes, false))));

    switch (FieldKey.wireType(key)) {
      case 1:
        double doubleValue = DoubleDecoder.decodeDouble(payloadBytes, 0);
        System.out.println(String.format("%sAsDouble: %f", indentation, doubleValue));
        break;
      case 2:
        System.out.println(String
            .format("%sASCII: %s", indentation, new String(copyBytes(data, protobufField), "UTF-8")));
        break;
    }
    System.out.println();

    if (recursive) {
      if (FieldKey.wireType(key) != 2) {
        return;
      }
      try {
        ProtobufObject protobufObject =
            decoder.decode(data, protobufField.valueStartOffset(), protobufField.valueEndOffset());
        prettyPrint(protobufObject, recursive, indent);
      } catch (Throwable t) {
        // There is no way to tell if the byte representation is primitive or it is a new proto object.
        // Use this as a hack for debugging now.
        // Don't log exception because we want pretty printing to be preserved, but this is dangerous.
        // TODO: Add this to the list of errors so that they are printed in the end.
        System.out.println(indentation
            + "Skipping recursion for the value of this field since it is not a valid proto message.");
        System.out.println();
        return;
      }
    }
  }

  private static byte[] copyBytes(byte[] data, ProtobufField protobufField) {
    return copyBytes(data, protobufField.valueStartOffset(), protobufField.valueEndOffset());
  }

  private static byte[] copyBytes(byte[] data, int start, int end) {
    byte[] bytes = new byte[end - start];
    for (int i = start; i < end; i++) {
      bytes[i - start] = data[i];
    }
    return bytes;
  }

  private static String makeIndentation(int indent) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }
}
