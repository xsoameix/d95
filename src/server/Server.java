package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {

    public static final int PIPE_EXIT = 0;
    public static final int PIPE_SYNC = 1;

    private static final int STATE_INITIAL = 0;
    private static final int STATE_RUNNING = 1;

    private Thread              thread;
    private Store               store;
    private Vector<InetAddress> IPTable;
    private Vector<Thread>      threads;
    private SinkChannel         ctrlIn;
    private SourceChannel       ctrlOut;
    private SinkChannel         syncIn;
    private SourceChannel       syncOut;
    private ServerSocketChannel server;
    private AtomicInteger       state = new AtomicInteger(STATE_INITIAL);

    public Server(Store store) {
        this.store = store;
    }

    public void initTCPServer() {
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            assert state.get() == STATE_INITIAL : "server should be initial";
            state.set(STATE_RUNNING);
            AtomicInteger uniqId  = new AtomicInteger(1);
            AtomicInteger counter = new AtomicInteger(1);
            IPTable = new Vector<InetAddress>();
            threads = new Vector<Thread>();
            threads.add(Thread.currentThread());
            Selector selector = Selector.open();
            Pipe ctrlPipe = Pipe.open();
            ctrlIn = ctrlPipe.sink();
            ctrlOut = ctrlPipe.source();
            ctrlOut.configureBlocking(false);
            ctrlOut.register(selector, SelectionKey.OP_READ);
            Pipe syncPipe = Pipe.open();
            syncIn = syncPipe.sink();
            syncOut = syncPipe.source();
            syncOut.configureBlocking(false);
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 5000);
            server.bind(addr);
            server.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int channels = selector.select();
                if (channels == 0) continue;
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        SocketChannel client = server.accept();
                        int id = uniqId.getAndIncrement();
                        Worker worker = new Worker(id, client,
                                IPTable, ctrlOut, counter, store);
                        Thread thread = new Thread(worker);
                        thread.start();
                        threads.add(thread);
                        counter.getAndIncrement();
                    } else if (key.isReadable()) {
                        ByteBuffer buf =
                            ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                        buf.putInt(PIPE_SYNC);
                        buf.flip();
                        syncIn.write(buf);
                        return;
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            assert state.get() == STATE_RUNNING : "server should be running";
            state.set(STATE_INITIAL);
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(PIPE_EXIT);
            buf.flip();
            ctrlIn.write(buf);
            Selector selector = Selector.open();
            syncOut.register(selector, SelectionKey.OP_READ);
            for (boolean loop = true; loop; ) {
                int channels = selector.select();
                if (channels == 0) continue;
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        synchronized(threads) {
                            Iterator<Thread> itor = threads.iterator();
                            while (itor.hasNext()) {
                                Thread thread = itor.next();
                                try {
                                    if (thread != Thread.currentThread()) {
                                        thread.join();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        loop = false;
                    }
                }
                selector.selectedKeys().clear();
            }
            server.close();
            buf.clear();
            syncOut.read(buf);
            syncIn.close();
            syncOut.close();
            buf.flip();
            ctrlOut.read(buf);
            ctrlIn.close();
            ctrlOut.close();
            threads.clear();
            IPTable.clear();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<InetAddress> getClientIPTable() {
        return IPTable;
    }
}
