package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        Lock          lock      = new ReentrantLock();
        Condition     isReady   = lock.newCondition();
        Condition     isLeaving = lock.newCondition();
        Store         store   = new Store();
        AtomicInteger uniqId  = new AtomicInteger(1);
        AtomicInteger counter = new AtomicInteger(0);
        Integer       max     = 2;
        ServerSocket  server;
        Socket client;
        Worker worker;
        int    id;
        new Timer().schedule(new Logger(store), 0, 100);
        try {
            server = new ServerSocket(40000);
            while (true) {
                lock.lock();
                try {
                    while (counter.get() == max)
                        isLeaving.await();
                    client = server.accept();
                    id     = uniqId.getAndIncrement();
                    worker = new Worker(id, client,
                            lock, isReady, isLeaving, counter, max, store);
                    new Thread(worker).start();
                    counter.getAndIncrement();
                    if (counter.get() == max)
                        isReady.signalAll();
                    System.out.println(
                            "new connection(" + counter.get() + ") from: " +
                            client.getRemoteSocketAddress() + "\n" +
                            "client(" + id + ") created");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
