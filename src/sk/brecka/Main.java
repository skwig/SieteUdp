package sk.brecka;

import sk.brecka.model.Packet;
import sk.brecka.thread.FileTransferThread;
import sk.brecka.thread.KeepAliveThread;
import sk.brecka.thread.ReceivingThread;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO discover hostov spusta timeout period
public class Main {
    static Scanner scanner;
    static String scannedLine;

    static Client client = null;

    private static void initFoo(int receivingPort, int sendingPort) throws IOException {
        client = new Client(receivingPort);
//        if (isStartingAsClient) {
//            System.out.println("Connect to IP");
//            scannedLine = scanner.nextLine();
//            client.setCurrentConnection(InetAddress.getByName(scannedLine));
//        }
//
        ReceivingThread receivingThread = new ReceivingThread(client);
        KeepAliveThread keepAliveThread = new KeepAliveThread(client);
        client.setReceivingThread(receivingThread);
        client.setKeepAliveThread(keepAliveThread);
        receivingThread.start();
        keepAliveThread.start();

//        if (isStartingAsClient) {
        MyDatagramSocket socket = client.getSocket();

        byte[] buf;

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
                    socket.sendNotifying(new DatagramPacket(buf, buf.length, InetAddress.getByName(inetAddress[0]), Integer.parseInt(inetAddress[1])),true);
                    break;
                case "M":
//                    List<Packet> packets = PacketFactory.createMessagePackets(input[1], 200);
//                    for (Packet p : packets) {
//                        buf = p.toBytes();
//                        socket.sendNotifying(new DatagramPacket(buf, buf.length, client.getCurrentConnection(), client.getSendingPort()),true);
//                    }


                    List<Packet> filePackets = PacketFactory.createMessagePackets(input[1], 200);
                    Packet transferPacket = filePackets.get(0);
                    filePackets.remove(0);

                    buf = transferPacket.toBytes();
                    client.setFileTransferThread(new FileTransferThread(client, transferPacket.getId(), filePackets));
                    socket.sendNotifying(new DatagramPacket(buf, buf.length, client.getCurrentConnection(), client.getSendingPort()),true);

                    break;
                case "F":
//                    List<Packet> filePackets = PacketFactory.createFilePackets(Paths.get(input[1]), 65490);
                    filePackets = PacketFactory.createFilePackets(Paths.get(input[1]), 2000);
                    transferPacket = filePackets.get(0);
                    filePackets.remove(0);

                    buf = transferPacket.toBytes();
                    client.setFileTransferThread(new FileTransferThread(client, transferPacket.getId(), filePackets));
                    socket.sendNotifying(new DatagramPacket(buf, buf.length, client.getCurrentConnection(), client.getSendingPort()),true);
                    break;
                case "H":
                    buf = PacketFactory.createFindHostsRequestPacket().toBytes();
//                    socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), Integer.parseInt(input[1])));
                    inetAddress = input[1].split(" ", 2);
                    // zavola broadcast pre podsiet
                    socket.sendNotifying(new DatagramPacket(buf, buf.length, InetAddress.getByName(inetAddress[0]), Integer.parseInt(inetAddress[1])),false);
                    break;
                case "HL":
                    buf = PacketFactory.createFindHostsRequestPacket().toBytes();

                    String address = InetAddress.getLocalHost().getHostAddress();
                    Pattern pattern = Pattern.compile("(.*\\.)");
                    Matcher matcher = pattern.matcher(address);

                    if (matcher.find()) {
                        socket.sendNotifying(new DatagramPacket(buf, buf.length, InetAddress.getByName(matcher.group().concat("255")), Integer.parseInt(input[1])),false);
                    } else {
                        System.out.println("Chyba ale nemalo by sa to stat");
                    }
                    break;
                case "E":
                    buf = PacketFactory.createFindHostsRequestPacket().toBytes();
                    buf[0]++;
                    socket.send(new DatagramPacket(buf,buf.length, client.getCurrentConnection(), client.getSendingPort()));
                    break;
                case "Q":
                    System.out.println("Quitting...");
                    System.exit(0);
                    break;
                case "I":
                    // trosku dojebane, nenajde vzdy ethernetovu siet
                    System.out.println("Your IP address is " + InetAddress.getLocalHost().getHostAddress());
//                    System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
//                    System.out.println(InetAddress.getLocalHost().getAddress());
//                    System.out.println(InetAddress.getLocalHost().getHostName());
                    break;
                default:
                    System.out.println("Incorrect input.\n" +
                            "C <ip> <port> to connect,\n" +
                            "M <message> to message,\n" +
                            "F <path> to sned a file,\n" +
                            "H <subnet> <port> to discover hosts,\n" +
                            "HL <port> to discover hosts on your local network,\n" +
                            "Q to quit");
                    break;
            }
        }
    }

//    private static void initServer() throws IOException {
//        client = new Client(PORT);
//
//        ServerReceivingThread receivingThread = new ServerReceivingThread(client);
//        client.setReceivingThread(receivingThread);
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
//        client = new Client(receivingPort, sendingPort);
////
//        ClientReceivingThread clientReceivingThread = new ClientReceivingThread(client);
//        client.setClientReceivingThread(clientReceivingThread);
//        clientReceivingThread.start();
//
//        DatagramSocket socket = client.getSocket();
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
//                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, client.getReceivingPort());
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
//boolean[] booleans = {false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true};
//
//        byte[] bytes = PacketStorage.booleanArrayToByteArray(booleans);
//
//        for (byte b : bytes) {
//
//            System.out.print(Integer.toBinaryString(b & 0xFF) + " ");
//        }
//
//        BitSet bitSet = new BitSet(16);
//
//        bitSet.set(0, true);
//        bitSet.set(1, true);
//        bitSet.set(2, true);
//        bitSet.set(3, true);
//        bitSet.set(5, true);
//        bitSet.set(9, true);
//        bitSet.set(10, true);
//
//        System.out.println(bitSet.toString());
//
//        for (byte b : bitSet.toByteArray()) {
//            System.out.print(b + " ");
//        }

    }
}

