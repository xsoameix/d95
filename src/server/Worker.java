package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Worker implements Runnable {
    int           id;
    Socket        socket;
    Lock          lock;
    Condition     isReady;
    Condition     isLeaving;
    AtomicInteger counter;
    Integer       max;
    Store         store;

    public Worker(
            int           id,
            Socket        socket,
            Lock          lock,
            Condition     isReady,
            Condition     isLeaving,
            AtomicInteger counter,
            Integer       max,
            Store         store) {
        this.id        = id;
        this.socket    = socket;
        this.lock      = lock;
        this.isReady   = isReady;
        this.isLeaving = isLeaving;
        this.counter   = counter;
        this.max       = max;
        this.store     = store;
    }

    public void run() {
        Hashtable<String, Integer> treasure = store.getTreasure();
        DataInputStream  in;
        DataOutputStream out;
        String  tokens[];
        String  action;
        String  name;
        Boolean haskey;

        // Restore all treasures if any client join.
        store.releaseAllTreasures();

        try {
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                lock.lock();
                try {
                    while (counter.get() < max)
                        isReady.await();
                } catch (InterruptedException e) {
                    store.releaseAllTreasures();
                    counter.getAndDecrement();
                    isLeaving.signal();
                    return;
                } finally {
                    lock.unlock();
                }
                tokens = in.readUTF().split(" ");
                action = tokens[0];
                name = tokens[1];
                synchronized(treasure) {
                    if (action.equals("GET")) {
                        haskey = treasure.containsKey(name);
                        if (haskey && treasure.get(name) == 0) {
                            treasure.put(name, id);
                            out.writeUTF(String.format(" %s %s", "YES", name));
                        } else {
                            out.writeUTF(String.format(" %s %s", "NO", name));
                        }
                    } else if (action.equals("RELEASE")) {
                        haskey = treasure.containsKey(name);
                        if (haskey && treasure.get(name) == id) {
                            treasure.put(name, 0);
                        }
                    }
                }
            }
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.printf("client(%d) removed\n", id);
            lock.lock();
            store.releaseAllTreasures();
            counter.getAndDecrement();
            isLeaving.signal();
            lock.unlock();
        }
    }
}
