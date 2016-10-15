package sk.brecka;

import sk.brecka.model.Packet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by Matej on 8.10.2016.
 */
public class PacketStorage {
    private List<Packet> packetList;
    private boolean isListening = false;
    private byte listenedForType = 0;
    private int listenedForGoalLength = 0;
    private int listenedForLength = 0;
    private int startId;

    public PacketStorage(int startId, byte listenedForType, int listenedForGoalLength) {
        this.packetList = new ArrayList<>();
        this.isListening = true;
        this.listenedForType = listenedForType;
        this.listenedForGoalLength = listenedForGoalLength;
        this.listenedForLength = 0;
        this.startId = startId;
    }

    public void addPacket(Packet p) throws NotOperatingException, IncorrectTypeException {
        if (isListening) {
            if (p.getType() == listenedForType) {
                packetList.add(p);
                listenedForLength += p.getData().length;
            } else {
                throw new IncorrectTypeException();
            }
        } else {
            throw new NotOperatingException();
        }
    }

    private int getMax() {
        int max = packetList.get(0).getId();
        for (Packet p : packetList) {
            if (p.getId() > max) {
                max = p.getId();
            }
        }
        return max;
    }

    public void buildAck() {
        boolean[] data = new boolean[getMax() - startId];
        Arrays.fill(data, false);

        for (Packet p : packetList) {
            data[p.getId() - startId] = true;
        }
    }

    public static byte[] booleanArrayToByteArray(boolean[] booleanArray) {
        byte byt = 0;


        int outSize = (int) Math.ceil(booleanArray.length / 8);
        int val;

        ByteBuffer byteBuffer = ByteBuffer.allocate(outSize);


        for (int i = 0; i < booleanArray.length; i++) {
            if (booleanArray[i]) {
                val = 1;
            } else {
                val = 0;
            }
            byt = (byte) ((byt << 1) | val);
            if ((i + 1) % 8 == 0) {
                byteBuffer.put(byt);
                byt = 0;
            }
        }
        return byteBuffer.array();
    }

    public String buildMessage() {
        Collections.sort(packetList, comparator);
        ByteBuffer byteBuffer = ByteBuffer.allocate(listenedForGoalLength);
        for (Packet p : packetList) {
            byteBuffer.put(p.getData());
        }
        isListening = false;
        return new String(byteBuffer.array());
    }

    public void buildFile() throws IOException {
        Collections.sort(packetList, comparator);

        ByteBuffer fileBuffer = ByteBuffer.allocate(listenedForGoalLength);
        for (Packet p : packetList) {
            fileBuffer.put(p.getData());
        }
        byte[] bytes = fileBuffer.array();

        ByteBuffer foo = ByteBuffer.wrap(bytes);

        int filenameLength = foo.getInt();
        String filename = "";

        for (int i = 0; i < filenameLength; i++) {
            filename += (char) foo.get();
        }

        String path = new String(filename);
        System.out.println("Building " + filename + "...");
        FileOutputStream fos = new FileOutputStream("downloads/" + path);
        byte[] bar = new byte[foo.remaining()];
        foo.get(bar);
        fos.write(bar);
//        for (byte b : fileBuffer.array()) {
//            System.out.print(b + " ");
//        }
        fos.close();
        System.out.println(filename + " built");
    }

    public boolean isFull() {
        return listenedForGoalLength == listenedForLength;
    }

    Comparator comparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            if (o1.getClass().equals(o2.getClass())) {
                if (o1 instanceof Packet) {
                    if (((Packet) o1).getType() == ((Packet) o2).getType()) {
                        return ((Packet) o1).getId() - ((Packet) o2).getId();
                    }
                }
            }
            return 0;
        }
    };


    public boolean isListening() {
        return isListening;
    }

    public byte getListenedForType() {
        return listenedForType;
    }

    public int getListenedForGoalLength() {
        return listenedForGoalLength;
    }

    public int getListenedForLength() {
        return listenedForLength;
    }

    public class IncorrectTypeException extends Exception {
    }

    public class NotOperatingException extends Exception {

    }
}
