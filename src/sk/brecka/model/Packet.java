package sk.brecka.model;

import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Created by Matej on 6.10.2016.
 */
public class Packet {
    public static final byte TRANSFER_START = 0x01;
    public static final byte MESSAGE = 0x02;
    public static final byte FILE = 0x03;
    public static final byte KEEP_ALIVE = 0x04;
    public static final byte RESPONSE_POSITIVE = 0x05;
    public static final byte RESPONSE_NEGATIVE = 0x06;

    public static final byte CONNECTION_START = 0x07;
    public static final byte CONNECTION_RESPONSE_BUSY = 0x08;
    public static final byte CONNECTION_RESPONSE_ACCEPTED = 0x09;
    public static final byte CONNECTION_ACKNOWLEDGE_RESPONSE = 0x0A;

    public static final byte DISCOVER_HOSTS_REQUEST = 0x0B;
    public static final byte DISCOVER_HOSTS_RESPONSE = 0x0C;

    public static final int CRC_LENGTH = Integer.BYTES;
    public static final int TYPE_LENGTH = Byte.BYTES;
    public static final int ID_LENGTH = Integer.BYTES;

    byte type;
    int id;
    byte[] data;

    boolean isAcknowledged;

    public Packet(int id, byte type, byte[] data) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.isAcknowledged = false;
    }


    public Packet() {
    }


    public static Packet fromBytes(byte[] bytes) throws MalformedInputException {
        // 8 = pocet bytov pre long
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long packetCrc = (long) byteBuffer.getInt();
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, Packet.CRC_LENGTH, bytes.length - Packet.CRC_LENGTH);
//        System.out.println("receivedCrc: " + packetCrc + ", realCrc: " + crc32.getValue());
        if (packetCrc == (int) crc32.getValue()) {
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

    public byte[] toBytes() {
        //
        byte[] data = ByteBuffer.allocate(this.getCrclessSize())
                .put(this.getType())
                .putInt(this.getId())
                .put(this.getData())
                .array();

        // create checksum
        CRC32 crc32 = new CRC32();
        crc32.update(data);

        // concat crc and data
        return ByteBuffer.allocate(this.getSize())
                .putInt((int) crc32.getValue())
                .put(data)
                .array();
    }


    public boolean isAcknowledged() {
        return isAcknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        isAcknowledged = acknowledged;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSize() {
        return data.length + CRC_LENGTH + TYPE_LENGTH + ID_LENGTH;
    }

    public int getCrclessSize() {
        return data.length + TYPE_LENGTH + ID_LENGTH;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", id=" + id +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
