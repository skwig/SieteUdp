package sk.brecka;

import sk.brecka.model.Packet;
import sk.brecka.thread.ReceivingThread;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static final int TIMEOUT_PERIOD_TOTAL = 2000;
    public static final int TIMEOUT_PERIOD = 1;

    public static final int PORT = 4444;

    static Scanner scanner;
    static String scannedLine;

    static Foo foo = null;

//    private static void serverOld() throws IOException {
//        DatagramSocket socket = new DatagramSocket(4445);
//
//        byte[] buf = new byte[2048];
//        DatagramPacket receivedDatagramPacket = new DatagramPacket(buf, buf.length);
//        DatagramPacket sentDatagramPacket;
//        System.out.println("Awaiting connection...");
//
//        PacketStorage packetStorage = null;
//
//        while (true) {
//            socket.receive(receivedDatagramPacket);
//            Packet receivedPacket;
//            try {
//                receivedPacket = PacketFactory.bytesToPacket(Arrays.copyOfRange(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength()));
//                System.out.println("received");
//
//                // odoslanie ack
//                byte[] response = PacketFactory.packetToBytes(PacketFactory.createPositiveResponsePacket(receivedPacket.getId()));
//                sentDatagramPacket = new DatagramPacket(response, response.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
//                socket.send(sentDatagramPacket);
//
//
//                switch (receivedPacket.getType()) {
//                    case Packet.TRANSFER_START:
//                        ByteBuffer byteBuffer = ByteBuffer.wrap(receivedPacket.getData());
//                        byte type = byteBuffer.get();
//                        packetStorage = new PacketStorage(type, byteBuffer.getInt());
//                        System.out.println("Transfer started, type: " + type);
//                        break;
//                    case Packet.UNFRAGMENTED_MESSAGE:
//                        serverPrintMessage(new String(receivedPacket.getData()));
//                        break;
//                    case Packet.FRAGMENTED_MESSAGE:
//                        if (packetStorage.isListening()) {
//                            try {
//                                packetStorage.addPacket(receivedPacket);
//                            } catch (PacketStorage.NotOperatingException | PacketStorage.IncorrectTypeException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        break;
//                    case Packet.FILE:
//                        if (packetStorage.isListening()) {
//                            try {
//                                packetStorage.addPacket(receivedPacket);
//                            } catch (PacketStorage.NotOperatingException | PacketStorage.IncorrectTypeException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        break;
//                }
//
//
//                if (packetStorage != null && packetStorage.isListening() && packetStorage.isFull()) {
//                    System.out.println("in");
//                    switch (packetStorage.getListenedForType()) {
//                        case Packet.FILE:
//                            packetStorage.buildFile();
//                            break;
//                        case Packet.FRAGMENTED_MESSAGE:
//                            serverPrintMessage(packetStorage.buildMessage());
//                            break;
//                    }
//                }
//
//            } catch (MalformedInputException e) {
//                e.printStackTrace();
//            }
////            switch (receivedPacket.getType()) {
////                case Packet.TRANSFER_START:
////                    listenedForType = ByteBuffer.wrap(receivedPacket.getData()).get();
////                    listenedForGoalLength = ByteBuffer.wrap(receivedPacket.getData()).getInt();
////                    listenedForLength = 0;
////                    break;
////                case Packet.FRAGMENTED_MESSAGE:
////                    if (receivedPacket.getType() == listenedForType) {
////                        listenedForLength += receivedPacket.getData().length;
////                    }
////
////                    if(listenedForGoalLength==listenedForLength){
////                        // hotovo, idem si to sortnut a pospajat
////                    }else if(listenedForGoalLength<listenedForLength){
////                        // dostal som viac bytov ako som mal
////                    }
////                    break;
////                case Packet.UNFRAGMENTED_MESSAGE:
////                    break;
////            }
//
//
//        }
//
////        buf = "foo".getBytes();
////        InetAddress address = receivedDatagramPacket.getAddress();
////        int PORT = receivedDatagramPacket.getReceivingPort();
////        receivedDatagramPacket = new DatagramPacket(buf, buf.length, address, PORT);
////        socket.send(receivedDatagramPacket);
//    }

    private static void serverPrintMessage(String message) {
        System.out.println("client: " + message);
    }

    private static void clientOld(String[] args) throws IOException {
        System.out.println("Connect to IP");
        scannedLine = scanner.nextLine();

        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[2048];

        InetAddress address = InetAddress.getByName(scannedLine);
        int packetLength = 2000;
        System.out.println("Send message:");
        while (true) {
            scannedLine = scanner.nextLine();

//            List<Packet> packetBatch = PacketFactory.createMessagePackets(scannedLine, packetLength);
//            File f = new File("fooIn.txt");
            List<Packet> packetBatch = PacketFactory.createFilePackets(Paths.get(scannedLine), packetLength);
            for (Packet p : packetBatch) {
                byte[] bytes = PacketFactory.packetToBytes(p);
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 4445);

                socket.send(packet);
                DatagramPacket response = new DatagramPacket(buf, buf.length);


                //cakanie na response
                int timeSlept = 0;
                while (true) {
                    try {
                        if (timeSlept >= TIMEOUT_PERIOD_TOTAL) {
                            System.out.println("Timed out. (" + TIMEOUT_PERIOD_TOTAL + ")");
                            break;
                        }

                        timeSlept += TIMEOUT_PERIOD;
                        Thread.sleep(TIMEOUT_PERIOD);

                        socket.receive(response);
                        try {
                            Packet responsePacket = PacketFactory.bytesToPacket(Arrays.copyOfRange(response.getData(), 0, response.getLength()));
                            if (responsePacket.getType() == Packet.RESPONSE_POSITIVE && ByteBuffer.wrap(responsePacket.getData()).getInt() == p.getId()) {
                                break;
                            }
                        } catch (MalformedInputException e) {
                            e.printStackTrace();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void server() throws IOException {
        byte[] byteArray = new byte[2048];

        DatagramSocket receivingDatagramSocket = new DatagramSocket(4445);

        DatagramPacket receivedDatagramPacket = new DatagramPacket(byteArray, byteArray.length);
        DatagramPacket sentDatagramPacket;

        InetAddress currentConnection = null;

        boolean isClientConnected = false;

        System.out.println("Awaiting connection...");
        // podmienku nahradit nejakou kontrolou zeci server neni vypnuty (v gui nejaky checkbox alebo daco)
        while (true) {
            receivingDatagramSocket.receive(receivedDatagramPacket);
            Packet receivedPacket;
            Packet sentPacket;

            try {
                receivedPacket = Packet.fromBytes(Arrays.copyOfRange(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength()));

                if (!isClientConnected) {
                    if (receivedPacket.getType() == Packet.CONNECTION_START) {
                        //
                        currentConnection = receivedDatagramPacket.getAddress();
                        isClientConnected = true;

                        byte[] sent = PacketFactory.createServerAcceptedPacket().toBytes();

                        sentDatagramPacket = new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                        receivingDatagramSocket.send(sentDatagramPacket);
                    }
                } else {
                    System.out.println("Received connection from " + currentConnection.toString());
                }
            } catch (MalformedInputException e) {
                e.printStackTrace();
            }

        }
    }

    private static void initFoo(int receivingPort, int sendingPort) throws IOException {
        foo = new Foo(receivingPort);
//        if (isStartingAsClient) {
//            System.out.println("Connect to IP");
//            scannedLine = scanner.nextLine();
//            foo.setCurrentConnection(InetAddress.getByName(scannedLine));
//        }
//
        ReceivingThread receivingThread = new ReceivingThread(foo);
        foo.setReceivingThread(receivingThread);
        receivingThread.start();

//        if (isStartingAsClient) {
        DatagramSocket socket = foo.getSocket();

        byte[] buf = new byte[2048];

        int packetLength = 2000;
        System.out.println("Send message:");
        boolean isRunning = true;
        while (isRunning) {
            scannedLine = scanner.nextLine();
            int spaceIndex = scannedLine.indexOf(' ');
            String[] input = scannedLine.split(" ", 2);

            switch (input[0].toUpperCase()) {
                case "C":
                    buf = PacketFactory.createClientConnectPacket().toBytes();
                    String[] inetAddress = input[1].split(" ", 2);
                    socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(inetAddress[0]), Integer.parseInt(inetAddress[1])));
                    break;
                case "M":
                    List<Packet> packets = PacketFactory.createMessagePackets(input[1], 200);
                    for (Packet p : packets) {
                        buf = p.toBytes();
                        socket.send(new DatagramPacket(buf, buf.length, foo.getCurrentConnection(), foo.getSendingPort()));
                    }
                    break;
                case "F":
                    List<Packet> filePackets = PacketFactory.createFilePackets(Paths.get(input[1]), 200);
                    for (Packet p : filePackets) {
                        buf = p.toBytes();
                        socket.send(new DatagramPacket(buf, buf.length, foo.getCurrentConnection(), foo.getSendingPort()));
                    }
                    break;
                case "H":
                    buf = PacketFactory.createFindHostsRequestPacket().toBytes();
//                    socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), Integer.parseInt(input[1])));
                    inetAddress = input[1].split(" ", 2);
                    // zavola broadcast pre podsiet
                    socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(inetAddress[0]), Integer.parseInt(inetAddress[1])));
                    break;
                case "HL":
                    buf = PacketFactory.createFindHostsRequestPacket().toBytes();

                    String address = InetAddress.getLocalHost().getHostAddress();
                    Pattern pattern = Pattern.compile("(.*\\.)");
                    Matcher matcher = pattern.matcher(address);

                    if (matcher.find()) {
                        socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(matcher.group().concat("255")), Integer.parseInt(input[1])));
                    } else {
                        System.out.println("Chyba ale nemalo by sa to stat");
                    }
                    break;
                case "Q":
                    System.out.println("Quitting...");
                    System.exit(0);
                    break;
                case "I":
                    System.out.println("Your IP address is " + InetAddress.getLocalHost().getHostAddress());
                    break;
                default:
                    System.out.println("Incorrect input. C to connect, M to message, F to file, H to discover hosts, Q to quit");
                    break;
            }
        }
    }

//    private static void initServer() throws IOException {
//        foo = new Foo(PORT);
//
//        ServerReceivingThread receivingThread = new ServerReceivingThread(foo);
//        foo.setReceivingThread(receivingThread);
//
//        receivingThread.start();
//    }
//
//    private static void initClient() throws IOException {
//
//        int receivingPort, sendingPort;
//
//        System.out.println("Connect to IP");
//        scannedLine = scanner.nextLine();
//        System.out.println("Send to port");
//        sendingPort = scanner.nextInt();
//        System.out.println("Receive from port");
//        receivingPort = scanner.nextInt();
//
//        foo = new Foo(receivingPort, sendingPort);
////
//        ClientReceivingThread clientReceivingThread = new ClientReceivingThread(foo);
//        foo.setClientReceivingThread(clientReceivingThread);
//        clientReceivingThread.start();
//
//        DatagramSocket socket = foo.getSocket();
//
//        byte[] buf = new byte[2048];
//
//        InetAddress address = InetAddress.getByName(scannedLine);
//        int packetLength = 2000;
//        System.out.println("Send message:");
//        while (true) {
//            scannedLine = scanner.nextLine();
//            if (scannedLine.equalsIgnoreCase("C")) {
//                buf = PacketFactory.createClientConnectPacket().toBytes();
//                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, foo.getReceivingPort());
//
//                socket.send(packet);
//            }
//        }
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int receivingPort, sendingPort;
        scanner = new Scanner(System.in);

        System.out.println("Receive on port:");
        scannedLine = scanner.nextLine();
        initFoo(Integer.parseInt(scannedLine), 0);

//        boolean[] booleans = {true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false};
//boolean[] booleans = {false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true};
//
//        byte[] bytes = PacketStorage.booleanArrayToByteArray(booleans);
//
//        for (byte b : bytes) {
//
//            System.out.print(Integer.toBinaryString(b & 0xFF) + " ");
//        }

//        receivingThread.
//        int length = 100;
//        List<Packet> packets = PacketFactory.createMessagePackets("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", length);
//        for (Packet p : packets) {
//            PacketFactory.bytesToPacket(PacketFactory.packetToBytes(p));
//        }

    }
}

