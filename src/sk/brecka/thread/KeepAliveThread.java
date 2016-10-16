package sk.brecka.thread;

import sk.brecka.Client;
import sk.brecka.PacketFactory;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by Matej on 13.10.2016.
 */
public class KeepAliveThread extends Thread {
    Client parent;

    boolean isRunning = false;
    long lastUpdate;

    public KeepAliveThread(Client parent) {
        this.parent = parent;
    }

    public void updateLastUpdate() {
        lastUpdate = System.currentTimeMillis();
        isRunning = true;
        if (!this.isAlive()) {
            this.start();
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Starting Keep alive thread");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (System.currentTimeMillis() > lastUpdate + Client.KEEP_ALIVE_PERIOD) {
                byte[] out = PacketFactory.createKeepAlivePacket().toBytes();
                try {
                    if (parent.getCurrentConnection() != null) {
//                        System.out.println("keeping alive");
                        parent.getSocket().sendNotifying(new DatagramPacket(out, out.length, parent.getCurrentConnection(), parent.getSendingPort()),true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
