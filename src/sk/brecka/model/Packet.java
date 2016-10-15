package sk.brecka.model;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Created by Matej on 6.10.2016.
 */
public class Packet {
    public static final byte UNFRAGMENTED_MESSAGE = 0x01;
    public static final byte KEEP_ALIVE = 0x03;
    public static final byte FRAGMENTED_MESSAGE = 0x04;
    public static final byte FILE = 0x05;
    public static final byte TRANSFER_START = 0x06;
    public static final byte RESPONSE_POSITIVE = 0x08;
    public static final byte RESPONSE_NEGATIVE = 0x09;

    public static final byte CONNECTION_START = 0x02;
    public static final byte CONNECTION_RESPONSE_BUSY = 0x0A;
    public static final byte CONNECTION_RESPONSE_ACCEPTED = 0x0B;
    public static final byte CONNECTION_ACKNOWLEDGE_RESPONSE = 0x0C;

    public static final byte DISCOVER_HOSTS_REQUEST = 0x0D;
    public static final byte DISCOVER_HOSTS_RESPONSE = 0x0E;

    public static final int CRC_LENGTH = 8;
    public static final int TYPE_LENGTH = 1;
    public static final int ID_LENGTH = 4;

    byte type;
    int id;
    byte[] data;

    public Packet(int id, byte type, byte[] data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }


    public Packet() {
    }

    public static Packet fromBytes(byte[] bytes) throws MalformedInputException {
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
                .putLong(crc32.getValue())
                .put(data)
                .array();
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
