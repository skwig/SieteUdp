package sk.brecka.thread;

import sk.brecka.Foo;
import sk.brecka.PacketFactory;
import sk.brecka.PacketStorage;
import sk.brecka.model.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;

/**
 * Created by Matej on 13.10.2016.
 */
public class ReceivingThread extends Thread {
    public static final int TIMEOUT_PERIOD = 2000;
    public static final int MAXIMUM_RETRY_COUNT = 5;
    Foo parent;

    int retryCount = 0;
    byte[] byteArray;
    DatagramPacket receivedDatagramPacket;
    DatagramPacket sentDatagramPacket;

    boolean hasConnection;

    PacketStorage packetStorage;

    public ReceivingThread(Foo parent) throws IOException {
        this.parent = parent;
        init();
    }

    private void init() throws IOException {
        byteArray = new byte[65536];
        receivedDatagramPacket = new DatagramPacket(byteArray, byteArray.length);
        hasConnection = false;
        packetStorage = null;
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println(this.getClass().getSimpleName() + " # start");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyTimeout() throws IOException {
        System.out.println("Connection timed out from " + parent.getCurrentConnection() + ", dropping");
        parent.setCurrentConnection(null);
        hasConnection = false;
    }

    private void notifyPacketStorage() throws IOException {
        if (packetStorage != null && packetStorage.isListening() && packetStorage.isFull()) {
            switch (packetStorage.getListenedForType()) {
                case Packet.FILE:
                    packetStorage.buildFile();
                    break;
                case Packet.FRAGMENTED_MESSAGE:
                    System.out.println((packetStorage.buildMessage()));
                    break;
            }
        }
    }

    private void sendAck(Packet p) throws IOException {
        byte[] buf = PacketFactory.createPositiveResponsePacket(p.getId()).toBytes();
        parent.getSocket().send(new DatagramPacket(buf, buf.length, parent.getCurrentConnection(), parent.getSendingPort()));
//        System.out.println("Sending ack");
    }

    private void doThread() throws IOException {
        try {
            parent.getSocket().receive(receivedDatagramPacket);
            retryCount = 0;
//            if (parent.getSocket().getSoTimeout() == 0) {
//                parent.getSocket().setSoTimeout(TIMEOUT_PERIOD);
//            }

            Packet receivedPacket;
            Packet sentPacket;
//            System.out.println("received from " + receivedDatagramPacket.getAddress() + ":" + receivedDatagramPacket.getPort());
            receivedPacket = Packet.fromBytes(Arrays.copyOfRange(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength()));
            byte[] sent;
            // nezavisle od existencie pripojenia
            switch (receivedPacket.getType()) {
                case Packet.CONNECTION_RESPONSE_ACCEPTED:
                    sent = PacketFactory.createClientAcknowledgedConnection().toBytes();
                    sentDatagramPacket = new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                    parent.getSocket().send(sentDatagramPacket);
                    hasConnection = true;
                    parent.setCurrentConnection(receivedDatagramPacket.getAddress());
                    parent.setSendingPort(receivedDatagramPacket.getPort());
                    System.out.println("Accepted by server, sending ack...");
                    break;
                case Packet.CONNECTION_RESPONSE_BUSY:
                    System.out.println("Refused by server, shutting down...");
                    break;
                case Packet.DISCOVER_HOSTS_REQUEST:
                    sent = PacketFactory.createFindHostsResponsePacket().toBytes();
                    sentDatagramPacket = new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                    parent.getSocket().send(sentDatagramPacket);
                    break;
                case Packet.DISCOVER_HOSTS_RESPONSE:
                    System.out.println("Host found at " + receivedDatagramPacket.getAddress() + ":" + receivedDatagramPacket.getPort());
                    break;
            }

            // zavisle od existencie pripojenia
            if (!hasConnection) {
                switch (receivedPacket.getType()) {
                    case Packet.CONNECTION_START:
                        sent = PacketFactory.createServerAcceptedPacket().toBytes();
                        sentDatagramPacket = new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                        parent.getSocket().send(sentDatagramPacket);
                        System.out.println("Received connection from " + receivedDatagramPacket.getAddress().toString());
                        break;

                    case Packet.CONNECTION_ACKNOWLEDGE_RESPONSE:
                        hasConnection = true;
                        parent.setCurrentConnection(receivedDatagramPacket.getAddress());
                        parent.setSendingPort(receivedDatagramPacket.getPort());
                        System.out.println("Connection acknowledged by " + receivedDatagramPacket.getAddress().toString());
                        break;
                }
            } else {
                switch (receivedPacket.getType()) {
                    case Packet.CONNECTION_START:
                        sent = PacketFactory.createServerBusyPacket().toBytes();
                        sentDatagramPacket = new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                        parent.getSocket().send(sentDatagramPacket);
                        System.out.println("Connection attempt from " + receivedDatagramPacket.getAddress().toString() + ", dropping");
                        break;

                    case Packet.UNFRAGMENTED_MESSAGE:
                        System.out.println("Received: " + new String(receivedPacket.getData()));
                        break;
                    case Packet.TRANSFER_START:
                        ByteBuffer byteBuffer = ByteBuffer.wrap(receivedPacket.getData());
                        byte type = byteBuffer.get();
                        packetStorage = new PacketStorage(receivedPacket.getId(), type, byteBuffer.getInt());

                        sendAck(receivedPacket);
                        System.out.println("Transfer started, type: " + type);
                        break;
                    case Packet.FILE:
                        if (packetStorage.isListening()) {
                            try {
//                                sent = PacketFactory.createPositiveResponsePacket(receivedPacket.getId()).toBytes();
                                if (!packetStorage.contains(receivedPacket.getId())) {
                                    packetStorage.addPacket(receivedPacket);
                                    this.notifyPacketStorage();
//                                parent.getSocket().send(new DatagramPacket(sent, sent.length, receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort()));
                                }
                            } catch (PacketStorage.NotOperatingException | PacketStorage.IncorrectTypeException e) {
                                e.printStackTrace();
                            }
                        }
                        sendAck(receivedPacket);
                        break;
                    case Packet.RESPONSE_POSITIVE:
//                        System.out.println("Got positive response");
                        int id = ByteBuffer.wrap(receivedPacket.getData()).getInt();
                        if (parent.getFileTransferThread().getTransferId() == id) {
                            parent.getFileTransferThread().start();
                        } else {
                            parent.getFileTransferThread().notifyAcknowledge(id);
                        }
                        break;

                    case Packet.FRAGMENTED_MESSAGE:
                        if (packetStorage.isListening()) {
                            try {
                                packetStorage.addPacket(receivedPacket);
                                this.notifyPacketStorage();
                            } catch (PacketStorage.NotOperatingException | PacketStorage.IncorrectTypeException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("No response");
            if (++retryCount > MAXIMUM_RETRY_COUNT) {
                notifyTimeout();
                if (parent.getSocket().getSoTimeout() != 0) {
                    parent.getSocket().setSoTimeout(0);
                }
            }
        } catch (MalformedInputException e) {
            e.printStackTrace();
        }

    }
}
