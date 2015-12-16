package server;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static client.Client.*;

import static suite.TestSuite.*;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestWorker {

    // Test whether Worker can handle movecode.
    @Test
    public void testHandlingMovecode() {
        try {
            final int expected[] = {
                GET,
                TURNEAST,
                TURNSOUTH,
                TURNNORTH,
                TURNWEST
            };
            AtomicInteger uniqId  = new AtomicInteger(1);
            AtomicInteger counter = new AtomicInteger(1);
            ServerSocketChannel server = ServerSocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 5000);
            server.bind(addr);
            Thread clientThread = new Thread() {
                public void run() {
                    try {
                        Socket socket = new Socket("127.0.0.1", 5000);
                        DataOutputStream out =
                            new DataOutputStream(socket.getOutputStream());
                        for (Integer movecode : expected) {
                            out.writeInt(movecode);
                        }
                        out.close();
                        socket.close();
                    } catch (Exception e) {
                    }
                }
            };
            clientThread.start();
            int id = uniqId.getAndIncrement();
            SocketChannel socket = server.accept();
            Vector<InetAddress> IPTable = new Vector<InetAddress>();
            Pipe ctrlPipe = Pipe.open();
            SinkChannel ctrlIn = ctrlPipe.sink();
            SourceChannel ctrlOut = ctrlPipe.source();
            ctrlOut.configureBlocking(false);
            final Vector<Integer> actual = new Vector<Integer>();
            Store store = new Store() {
                public void getItem(int clientno) {
                    actual.add(GET);
                }
                public void updateDirection(int clientno, int movecode) {
                    actual.add(movecode);
                }
            };
            Worker worker = new Worker(id, socket,
                    IPTable, ctrlOut, counter, store);
            Thread workerThread = new Thread(worker);
            workerThread.start();
            Thread.sleep(TRANSFER_TIME);
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(Server.PIPE_EXIT);
            buf.flip();
            ctrlIn.write(buf);
            workerThread.join();
            for (int i = 0; i < expected.length; i++) {
                assertEquals(actual.get(i).intValue(), expected[i]);
            }
            ctrlIn.close();
            ctrlOut.close();
            clientThread.join();
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
