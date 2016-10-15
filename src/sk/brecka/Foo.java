package sk.brecka;

import sk.brecka.thread.FileTransferThread;
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
    FileTransferThread fileTransferThread;

    public Foo(int receivingPort) throws IOException {
        this.receivingPort = receivingPort;
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

    public FileTransferThread getFileTransferThread() {
        return fileTransferThread;
    }

    public void setFileTransferThread(FileTransferThread fileTransferThread) {
        this.fileTransferThread = fileTransferThread;
    }
}
