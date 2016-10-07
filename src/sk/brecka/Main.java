package sk.brecka;

import sk.brecka.model.Packet;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final int MAXIMUM_PACKET_LENGTH = 20;


    byte[] sentData = new byte[1024];
    byte[] receivedData = new byte[1024];
    static Scanner scanner;
    static String scannedLine;

    private static void server() throws IOException {
        DatagramSocket socket = new DatagramSocket(4445);

        byte[] buf = new byte[128];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        System.out.println("Awaiting connection...");
        List<Packet> packetHeap = new ArrayList<>();

        byte listenedForType = 0;
        int listenedForGoalLength = 0;
        int listenedForLength = 0;
        while (true) {
            socket.receive(packet);
            Packet receivedPacket = PacketFactory.bytesToPacket(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
//            switch (receivedPacket.getType()) {
//                case Packet.TRANSFER_START:
//                    listenedForType = ByteBuffer.wrap(receivedPacket.getData()).get();
//                    listenedForGoalLength = ByteBuffer.wrap(receivedPacket.getData()).getInt();
//                    listenedForLength = 0;
//                    break;
//                case Packet.FRAGMENTED_MESSAGE:
//                    if (receivedPacket.getType() == listenedForType) {
//                        listenedForLength += receivedPacket.getData().length;
//                    }
//
//                    if(listenedForGoalLength==listenedForLength){
//                        // hotovo, idem si to sortnut a pospajat
//                    }else if(listenedForGoalLength<listenedForLength){
//                        // dostal som viac bytov ako som mal
//                    }
//                    break;
//                case Packet.UNFRAGMENTED_MESSAGE:
//                    break;
//            }


            packetHeap.add(receivedPacket);

        }

//        buf = "foo".getBytes();
//        InetAddress address = packet.getAddress();
//        int port = packet.getPort();
//        packet = new DatagramPacket(buf, buf.length, address, port);
//        socket.send(packet);
    }

    private static void client(String[] args) throws IOException {
        System.out.println("Connect to IP");
        scannedLine = scanner.nextLine();

        DatagramSocket socket = new DatagramSocket();


        InetAddress address = InetAddress.getByName(scannedLine);
        int packetLength = 20;
        System.out.println("Send message:");
        while(true) {
            scannedLine = scanner.nextLine();

            List<Packet> packetBatch = PacketFactory.createMessagePackets(scannedLine, packetLength);
            for (Packet p : packetBatch) {
                byte[] bytes = PacketFactory.packetToBytes(p);
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 4445);
                socket.send(packet);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        System.out.println("C for client, S for server");
        scannedLine = scanner.nextLine();
        if (scannedLine.equalsIgnoreCase("C")) {
            client(args);
        } else if (scannedLine.equalsIgnoreCase("S")) {
            server();
        }

//        int length = 100;
//        List<Packet> packets = PacketFactory.createMessagePackets("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", length);
//        for (Packet p : packets) {
//            PacketFactory.bytesToPacket(PacketFactory.packetToBytes(p));
//        }

    }
}

