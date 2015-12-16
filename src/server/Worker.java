package server;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static client.Client.*;

public class Worker implements Runnable {
    int                 id;
    SocketChannel       socket;
    Vector<InetAddress> IPTable;
    SourceChannel       ctrlOut;
    AtomicInteger       counter;
    Store               store;

    public Worker(
            int                 id,
            SocketChannel       socket,
            Vector<InetAddress> IPTable,
            SourceChannel       ctrlOut,
            AtomicInteger       counter,
            Store               store) {
        this.id        = id;
        this.socket    = socket;
        this.IPTable   = IPTable;
        this.ctrlOut   = ctrlOut;
        this.counter   = counter;
        this.store     = store;
    }

    public void run() {
        try {
            IPTable.add(socket.socket().getInetAddress());
            Selector selector = Selector.open();
            ctrlOut.register(selector, SelectionKey.OP_READ);
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
            while (true) {
                int channels = selector.select();
                if (channels == 0) continue;
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        if (key.channel().equals(socket)) {
                            int read = socket.read(buf);
                            if (read == -1) return;
                            if (buf.hasRemaining()) continue;
                            buf.flip();
                            int movecode = buf.getInt();
                            switch (movecode) {
                                case GET:
                                    store.getItem(id);
                                    break;
                                case TURNEAST:
                                case TURNSOUTH:
                                case TURNNORTH:
                                case TURNWEST:
                                    store.updateDirection(id, movecode);
                                    break;
                            }
                            buf.clear();
                        } else if (key.channel().equals(ctrlOut)) {
                            socket.close();
                            return;
                        }
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            counter.getAndDecrement();
            IPTable.remove(socket.socket().getInetAddress());
        }
    }
}
