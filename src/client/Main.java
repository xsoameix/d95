package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        Store store = new Store();
        ArrayList<String>          names    = store.getKeys();
        Hashtable<String, Integer> treasure = store.getTreasure();
        Socket           socket;
        DataOutputStream out;
        DataInputStream  in;
        HashSet<String> keys;
        String send;
        String recv;
        Integer seconds;
        new Timer().schedule(new Logger(store), 0, 100);
        try {
            socket = new Socket("127.0.0.1", 40000);
            out = new DataOutputStream(socket.getOutputStream());
            in  = new DataInputStream(socket.getInputStream());
            while (true) {
                for (String name : names) {
                    synchronized(treasure) {
                        if (!treasure.containsKey(name)) {
                            send = String.format("%s %s", "GET", name);
                            recv = String.format(" %s %s", "YES", name);
                            out.writeUTF(send);
                            if (in.readUTF().equals(recv)) {
                                treasure.put(name, 5);
                            }
                        }
                    }
                    Thread.sleep(1000);
                    synchronized(treasure) {
                        keys = new HashSet<String>(treasure.keySet());
                        for (String key : keys) {
                            seconds = treasure.get(key) - 1;
                            if (seconds > 0) {
                                treasure.put(key, seconds);
                            } else {
                                treasure.remove(key);
                                send = String.format("%s %s", "RELEASE", key);
                                out.writeUTF(send);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("disconnected from server");
        }
    }
}
