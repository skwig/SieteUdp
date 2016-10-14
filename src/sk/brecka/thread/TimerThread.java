package sk.brecka.thread;

import java.io.IOException;

/**
 * Created by Matej on 13.10.2016.
 */
public class TimerThread extends Thread {
    ReceivingThread parent;

    boolean isRunning = false;

    long lastUpdate;
    int timeout = 2000;

    public TimerThread(ReceivingThread parent) {
        this.parent = parent;
    }

    public void updateLastUpdate() {
        lastUpdate = System.currentTimeMillis();
        isRunning = true;
        if (!this.isAlive()) {
            this.start();
        }
    }

    private void notifyParent() throws IOException {
        if (isRunning) {
            parent.notifyTimeout();
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        System.out.println("Starting Timer Thread");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (System.currentTimeMillis() > lastUpdate + timeout) {
                try {
                    parent.notifyTimeout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
