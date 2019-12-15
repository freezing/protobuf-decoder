package io;

import com.google.protobuf.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import samples.Protos.AddressBook;
import samples.Protos.Coordinates;
import samples.Protos.Person;
import samples.Protos.Person.PhoneNumber;
import samples.Protos.Person.PhoneType;
import samples.Protos.Position;
import samples.Protos.TestMessage;
import samples.Protos.Value;

public class ProtoDumper {

  public static void main(String[] args) throws IOException {
    mainAddressBook(args);
    mainPosition(args);
    mainTestMessage(args);
  }

  private static void mainPosition(String[] args) throws IOException {
    Position position = Position.newBuilder()
        .setCoordinates(Coordinates.newBuilder().setX(567.24).setY(-52678.11).setZ(44))
        .build();
    dumpToFile(position, DebuggingConfig.POSITION_PROTO_DUMP);
  }

  private static void mainAddressBook(String[] args) throws IOException {
    Person person1 = Person.newBuilder()
        .setId(1)
        .setName("Nikola")
        .setEmail("nikolavla@gmail.com")
        .addPhones(PhoneNumber.newBuilder().setType(PhoneType.HOME).setNumber("0643868378"))
        .build();
    Person person2 = Person.newBuilder()
        .setId(2)
        .setName("Sara")
        .setEmail("sararacic@gmail.com")
        .addPhones(PhoneNumber.newBuilder().setType(PhoneType.MOBILE).setNumber("0641506094"))
        .build();
    AddressBook addressBook = AddressBook.newBuilder()
        .addPeople(person1)
        .addPeople(person2)
        .build();

    dumpToFile(addressBook, DebuggingConfig.ADDRESS_BOOK_PROTO_DUMP);
  }

  private static void mainTestMessage(String[] args) throws IOException {
    TestMessage testMessage = TestMessage.newBuilder()
        .putIntMap(1, Value.newBuilder().setData(1.0).build())
        .putIntMap(2, Value.newBuilder().setData(2999.0).build())
        .putIntMap(-3, Value.newBuilder().setData(-134.55).build())
        .putStringMap("String1", Value.newBuilder().setData(-1).build())
        .putStringMap("String2", Value.newBuilder().setData(-2).build())
        .putStringMap("String3", Value.newBuilder().setData(-3).build())
        .build();
    dumpToFile(testMessage, DebuggingConfig.TEST_MESSAGE_PROTO_DUMP);
  }

  private static void dumpToFile(Message message, String filepath) throws IOException {
    Path path = Paths.get(filepath);
    OutputStream outputStream = Files.newOutputStream(path);
    message.writeTo(outputStream);
    outputStream.close();
  }
}
