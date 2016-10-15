package sk.brecka;

import sk.brecka.model.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by Matej on 6.10.2016.
 */
public class PacketFactory {
    static int packetId = 0;

    public static List<Packet> createMessagePackets(String message, int packetLength) {
        if (packetLength > message.length()) {
            return Arrays.asList(PacketFactory.createMessagePacket(message));
        } else {

            List<Packet> packets = new ArrayList<>();
            packets.add(PacketFactory.createTransferStartPacket(Packet.FRAGMENTED_MESSAGE, message.getBytes().length));
            for (int i = 0; i < message.length() / packetLength + 1; i++) {
                // urci dlzku packetu
                int length;
                if ((i + 1) * packetLength < message.length()) {
                    length = packetLength;
                } else {
                    length = message.length() - i * packetLength;
                }

                String messageFragment = message.substring(i * packetLength, i * packetLength + length);
                Packet packet = new Packet();
                packet.setId(generatePacketId());
                packet.setType(Packet.FRAGMENTED_MESSAGE);
                packet.setData(messageFragment.getBytes());

                packets.add(packet);
            }
            return packets;
        }
    }

    public static List<Packet> createFilePackets(Path path, int packetLength) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        byte[] filenameBytes = path.getFileName().toString().getBytes();

        byte[] joined = ByteBuffer.allocate(fileBytes.length + filenameBytes.length + 4)
                .putInt(filenameBytes.length)
                .put(filenameBytes)
                .put(fileBytes)
                .array();


        List<Packet> packets = new ArrayList<>();

        packets.add(PacketFactory.createTransferStartPacket(Packet.FILE, joined.length));
        for (int i = 0; i < joined.length / packetLength + 1; i++) {
            // urci dlzku packetu
            int length;
            if ((i + 1) * packetLength < joined.length) {
                length = packetLength;
            } else {
                length = joined.length - i * packetLength;
            }
            byte[] byteFragment = Arrays.copyOfRange(joined, i * packetLength, i * packetLength + length);
            Packet packet = new Packet();
            packet.setId(generatePacketId());
            packet.setType(Packet.FILE);
            packet.setData(byteFragment);

            packets.add(packet);
        }
        return packets;
    }

    public static Packet createFindHostsRequestPacket() {
        return new Packet(generatePacketId(), Packet.DISCOVER_HOSTS_REQUEST, new byte[0]);
    }
    public static Packet createFindHostsResponsePacket() {
        return new Packet(generatePacketId(), Packet.DISCOVER_HOSTS_RESPONSE, new byte[0]);
    }

    public static Packet createMessagePacket(String message) {ł
        return new Packet(generatePacketId(), Packet.UNFRAGMENTED_MESSAGE, message.getBytes());
    }

    public static Packet createTransferStartPacket(byte messageType, int messageByteLength) {
        byte[] data = ByteBuffer.allocate(Packet.ID_LENGTH + Packet.TYPE_LENGTH)
                .put(messageType)
                .putInt(messageByteLength)
                .array();
        return new Packet(generatePacketId(), Packet.TRANSFER_START, data);
    }

    public static Packet createServerBusyPacket() {
        return new Packet(generatePacketId(), Packet.CONNECTION_RESPONSE_BUSY, new byte[0]);
    }

    public static Packet createServerAcceptedPacket() {
        return new Packet(generatePacketId(), Packet.CONNECTION_RESPONSE_ACCEPTED, new byte[0]);
    }

    public static Packet createClientConnectPacket() {
        return new Packet(generatePacketId(), Packet.CONNECTION_START, new byte[0]);
    }

//    public static Packet createFileTransferStartPacket(byte messageType, int messageByteLength) {
//        byte[] data = ByteBuffer.allocate(Packet.ID_LENGTH + Packet.TYPE_LENGTH)
//                .put(messageType)
//                .putInt(messageByteLength)
//                .array();
//        return new Packet(generatePacketId(), Packet.TRANSFER_FILE_START, data);
//    }

    public static Packet createClientAcknowledgedConnection() {
        return new Packet(generatePacketId(), Packet.CONNECTION_ACKNOWLEDGE_RESPONSE, new byte[0]);
    }

    public static Packet createPositiveResponsePacket(int id) {
        return new Packet(generatePacketId(), Packet.RESPONSE_POSITIVE, ByteBuffer.allocate(Packet.ID_LENGTH).putInt(id).array());
    }

    @Deprecated
    public static byte[] packetToBytes(Packet packet) {
        //
        byte[] data = ByteBuffer.allocate(packet.getCrclessSize())
                .put(packet.getType())
                .putInt(packet.getId())
                .put(packet.getData())
                .array();

        // create checksum
        CRC32 crc32 = new CRC32();
        crc32.update(data);

        // concat crc and data
        return ByteBuffer.allocate(packet.getSize())
                .putLong(crc32.getValue())
                .put(data)
                .array();
    }

    @Deprecated
    public static Packet bytesToPacket(byte[] bytes) throws MalformedInputException {
        // 8 = pocet bytov pre long
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long packetCrc = byteBuffer.getLong();
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, Packet.CRC_LENGTH, bytes.length - Packet.CRC_LENGTH);
//        System.out.println("receivedCrc: " + packetCrc + ", realCrc: " + crc32.getValue());
        if (packetCrc == crc32.getValue()) {
            Packet packet = new Packet();

            // typ
            packet.setType(byteBuffer.get());

            // id
            packet.setId(byteBuffer.getInt());

            // data
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            packet.setData(data);

//            System.out.println(new String(packet.getData()));
            return packet;
        } else {
            throw new MalformedInputException(0);
        }
    }

    public static int generatePacketId() {
        return packetId++;
    }
}
