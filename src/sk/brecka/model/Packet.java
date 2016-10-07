package sk.brecka.model;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.Arrays;

/**
 * Created by Matej on 6.10.2016.
 */
public class Packet {
    public static final byte UNFRAGMENTED_MESSAGE = 0x01;
    public static final byte FRAGMENTED_MESSAGE = 0x02;
    public static final byte UNFRAGMENTED_FILE = 0x03;
    public static final byte FRAGMENTED_FILE = 0x04;
    public static final byte TRANSFER_START = 0x05;
    public static final byte RESPONSE_POSITIVE = 0x06;
    public static final byte RESPONSE_NEGATIVE = 0x07;

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
