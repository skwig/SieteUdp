package sk.brecka;

import sk.brecka.thread.FileTransferThread;
import sk.brecka.thread.KeepAliveThread;
import sk.brecka.thread.ReceivingThread;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Matej on 12.10.2016.
 */
public class Client {
    public static final int TIMEOUT_PERIOD = 3000;
    public static final int MAXIMUM_RETRY_COUNT = 10;
    public static final int KEEP_ALIVE_PERIOD = 10_000;


    MyDatagramSocket socket;
    int receivingPort;
    int sendingPort;

    InetAddress currentConnection;

    ReceivingThread receivingThread;
    FileTransferThread fileTransferThread;
    KeepAliveThread keepAliveThread;

    public Client(int receivingPort) throws IOException {
        this.receivingPort = receivingPort;
        this.currentConnection = null;
        this.socket = new MyDatagramSocket(this,receivingPort);
    }

    public int getSendingPort() {
        return sendingPort;
    }

    public void setSendingPort(int sendingPort) {
        this.sendingPort = sendingPort;
    }

    public MyDatagramSocket getSocket() {
        return socket;
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

    public KeepAliveThread getKeepAliveThread() {
        return keepAliveThread;
    }

    public void setKeepAliveThread(KeepAliveThread keepAliveThread) {
        this.keepAliveThread = keepAliveThread;
    }
}
