package sk.brecka.thread;

import sk.brecka.Client;
import sk.brecka.model.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matej on 15.10.2016.
 */
public class FileTransferThread extends Thread {
//    public static final int SLEEP_PERIOD = 1;

    Client parent;

    List<Packet> transferredPackets;
    List<DatagramPacket> transferredDatagramPackets;
    boolean beingTransferred;
    int acknowledgedCount;
    int transferId;

    long transferStart;

    public FileTransferThread(Client parent, int transferId, List<Packet> transferredPackets) {
        this.parent = parent;
        this.transferredPackets = transferredPackets;
        this.transferredDatagramPackets = new ArrayList<>();
        byte[] buf;
        for (Packet p : transferredPackets) {
            buf = p.toBytes();
            transferredDatagramPackets.add(new DatagramPacket(buf, buf.length, parent.getCurrentConnection(), parent.getSendingPort()));
        }

        this.beingTransferred = false;
        this.transferId = transferId;
        this.acknowledgedCount = 0;
    }

    @Override
    public synchronized void start() {
        super.start();
        this.beingTransferred = true;
        this.transferStart = System.currentTimeMillis();
        System.out.println("File transfer starting @ " + transferStart);
    }

    @Override
    public void run() {
        byte[] buf;
        while (beingTransferred) {
            System.out.println(acknowledgedCount + "/" + transferredPackets.size());
            if (acknowledgedCount == transferredPackets.size()) {
                beingTransferred = false;
                System.out.println("Ended in " + (System.currentTimeMillis() - transferStart) + "ms");
                break;
            }

            for (int i = 0; i < transferredPackets.size(); i++) {
                if (!transferredPackets.get(i).isAcknowledged()) {
                    try {
                        parent.getSocket().sendNotifying(transferredDatagramPackets.get(i),true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // sleepni aby mohol pockat na vsetky acky
//            try {
//                Thread.sleep(SLEEP_PERIOD);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void notifyAcknowledge(int id) {
        for (Packet p : transferredPackets) {
            if (p.getId() == id && !p.isAcknowledged()) {
//                System.out.println("acknowledged packet");
                p.setAcknowledged(true);

                acknowledgedCount++;
                return;
            }
        }
    }

    public int getTransferId() {
        return transferId;
    }

}
