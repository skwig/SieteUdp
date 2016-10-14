package sk.brecka;

import sk.brecka.thread.ReceivingThread;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Matej on 12.10.2016.
 */
public class Foo {
    DatagramSocket socket;
    int receivingPort;
    int sendingPort;

    InetAddress currentConnection;

    ReceivingThread receivingThread;

    public Foo(int receivingPort, int sendingPort) throws IOException {
        this.receivingPort = receivingPort;
        this.sendingPort = sendingPort;
        this.socket = new DatagramSocket(receivingPort);
        this.currentConnection = null;
    }

    public int getSendingPort() {
        return sendingPort;
    }

    public void setSendingPort(int sendingPort) {
        this.sendingPort = sendingPort;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public ReceivingThread getReceivingThread() {
        return receivingThread;
    }

    public void setReceivingThread(ReceivingThread receivingThread) {
        this.receivingThread = receivingThread;
    }

    public InetAddress getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(InetAddress currentConnection) {
        this.currentConnection = currentConnection;
    }
}
