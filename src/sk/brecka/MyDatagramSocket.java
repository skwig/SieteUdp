package sk.brecka;

import java.io.IOException;
import java.net.*;

/**
 * Created by Matej on 16.10.2016.
 */
public class MyDatagramSocket extends DatagramSocket {
    Client parent;
    public MyDatagramSocket(Client parent) throws SocketException {
        this.parent = parent;
    }

    public MyDatagramSocket(Client parent, DatagramSocketImpl impl) {
        super(impl);
        this.parent = parent;
    }

    public MyDatagramSocket(Client parent, SocketAddress bindaddr) throws SocketException {
        super(bindaddr);
        this.parent = parent;
    }

    public MyDatagramSocket(Client parent, int port) throws SocketException {
        super(port);
        this.parent = parent;
    }

    public MyDatagramSocket(Client parent, int port, InetAddress laddr) throws SocketException {
        super(port, laddr);
        this.parent = parent;
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        super.send(p);
    }

    public void sendNotifying(DatagramPacket p, boolean notify) throws IOException{
        this.send(p);
        if(notify){
            parent.getKeepAliveThread().updateLastUpdate();
        }
    }
}
