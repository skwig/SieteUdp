package sk.brecka;

import sk.brecka.model.Packet;

import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
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

    public static Packet createMessagePacket(String message) {
        return new Packet(generatePacketId(),Packet.UNFRAGMENTED_MESSAGE,message.getBytes());
    }

    public static Packet createPositiveResponsePacket(int id) {
        return new Packet(generatePacketId(), Packet.RESPONSE_POSITIVE, ByteBuffer.allocate(Packet.ID_LENGTH).putInt(id).array());
    }

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

    public static Packet bytesToPacket(byte[] bytes) throws MalformedInputException {
        // 8 = pocet bytov pre long
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long packetCrc = byteBuffer.getLong();
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, 8, bytes.length - 8);
        System.out.println("receivedCrc: " + packetCrc +", realCrc: " + crc32.getValue());
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

            System.out.println(new String(packet.getData()));
            return packet;
        } else {
            throw new MalformedInputException(0);
        }
    }

    public static int generatePacketId() {
        return packetId++;
    }
}
