package io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.component.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import samples.Protos.AddressBook;
import samples.Protos.Coordinates;
import samples.Protos.Position;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DecoderBenchmark {

  private static final Path addressBookPath = Paths.get("/tmp/protodump-addressbook.bin");
  private static final Path positionPath = Paths.get("/tmp/protodump-position.bin");

  private Decoder decoder;
  private byte[] addressBookData;

  Position position1;
  Position position2;

  ProtobufObject protobufObjectPosition1;
  ProtobufObject protobufObjectPosition2;

  Component component;

  private byte[] positionData1;
  private byte[] positionData2;

  private double[] doubleValues = new double[3];

  @Setup
  public void setup() throws IOException {
    decoder = new Decoder();

    addressBookData = Files.readAllBytes(addressBookPath);
    positionData1 = Files.readAllBytes(positionPath);

    position1 = Position.parseFrom(positionData1);
    position2 = Position.newBuilder().setCoordinates(Coordinates.newBuilder().setX(51).setY(5914678.3245).setZ(-1245135.33)).build();

    positionData2 = position2.toByteArray();

    protobufObjectPosition1 = decoder.decode(positionData1, 0, positionData1.length);
    protobufObjectPosition2 = decoder.decode(positionData2, 0, positionData2.length);

    component = new Component(protobufObjectPosition1);
  }

  @Benchmark
  public ProtobufObject customDecoder() {
    return decoder.decode(positionData1, 0, positionData1.length);
  }

  @Benchmark
  public AddressBook officialDecoderAddressBook() throws InvalidProtocolBufferException {
    return AddressBook.parseFrom(addressBookData);
  }

  @Benchmark
  public Position officialDecoderPosition() throws InvalidProtocolBufferException {
    return Position.parseFrom(positionData1);
  }

  @Benchmark
  public double officialDecodeAndSumCoordinates() throws InvalidProtocolBufferException {
    Position position = Position.parseFrom(positionData1);
    Coordinates coordinates = position.getCoordinates();
    return coordinates.getX() + coordinates.getY() + coordinates.getZ();
  }

  @Benchmark
  public double customDecodeAndSumCoordinates() {
    ProtobufObject protobufObject = decoder.decode(positionData1, 0, positionData1.length);
    ProtobufField coordinatesField = protobufObject.fields().array()[0];
    ProtobufObject coordinatesObject = decoder.decode(positionData1, coordinatesField.valueStartOffset(), coordinatesField.valueEndOffset());
    for (int i = 0; i < coordinatesObject.fields().size(); i++) {
      ProtobufField field = coordinatesObject.fields().array()[i];
      int fieldId = FieldKey.fieldId(field.key());
      doubleValues[fieldId - 1] = DoubleDecoder.decodeDouble(positionData1, field.valueStartOffset());
    }
    return doubleValues[0] + doubleValues[1] + doubleValues[2];
  }

  @Benchmark
  public Position officialPositionMerge() {
    return position1.toBuilder().mergeFrom(position2).build();
  }

  @Benchmark
  public Component customProtobufObjectMerge() {
    component.merge(protobufObjectPosition2);
    return component;
  }
}
