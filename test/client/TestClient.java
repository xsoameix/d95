package client;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static suite.TestSuite.*;

import static client.Client.*;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestClient {

    // Test connectServer() method.
    @Test
    public void testConnectServer() {
        try {
            final AtomicBoolean called = new AtomicBoolean(false);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ServerSocket server = new ServerSocket(5000);
                        Socket socket = server.accept();
                        socket.close();
                        server.close();
                        called.set(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            Client client = new Client();
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            boolean successful = client.connectServer(addr);
            Thread.sleep(TRANSFER_TIME);
            assertTrue(successful);
            assertTrue(called.get());
            client.close();
            thread.join();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test inputMove() method.
    @Test
    public void testInputMove() {
        try {
            final int expected[] = {
                GET,
                TURNEAST,
                TURNSOUTH,
                TURNNORTH,
                TURNWEST
            };
            final Vector<Integer> actual = new Vector<Integer>();
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ServerSocket server = new ServerSocket(5000);
                        Socket socket = server.accept();
                        DataInputStream in =
                            new DataInputStream(socket.getInputStream());
                        for (int i = 0; i < expected.length; i++) {
                            int read = in.readInt();
                            actual.add(read);
                        }
                        in.close();
                        socket.close();
                        server.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            Client client = new Client();
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            Thread.sleep(TRANSFER_TIME);
            client.connectServer(addr);
            for (Integer movecode : expected) {
                client.inputMove(movecode);
            }
            client.close();
            thread.join();
            assertEquals(actual.size(), expected.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(actual.get(i).intValue(), expected[i]);
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test whether Client has a assertion that
    // Client shoule close the previous connection before connecting to Server.
    @Test(expected = AssertionError.class)
    public void testConnectServerTwice() {
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ServerSocket server = new ServerSocket(5000);
                        Socket socket = server.accept();
                        socket.close();
                        server.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            Client client = new Client();
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            Thread.sleep(TRANSFER_TIME);
            client.connectServer(addr);
            try {
                client.connectServer(addr);
            } finally {
                client.close();
                thread.join();
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test whether Client has a assertion that
    // Client shoule connect to Server before sending movecode.
    @Test(expected = AssertionError.class)
    public void testInputMoveBeforeConnectServer() {
        Client client = new Client();
        client.inputMove(Client.GET);
    }

    // Test whether Client has a assertion that
    // Client shoule connect to Server before being closed.
    @Test(expected = AssertionError.class)
    public void testCloseTwice() {
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ServerSocket server = new ServerSocket(5000);
                        Socket socket = server.accept();
                        socket.close();
                        server.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            Client client = new Client();
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            Thread.sleep(TRANSFER_TIME);
            client.connectServer(addr);
            client.close();
            try {
                client.close();
            } finally {
                thread.join();
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test whether Client has a assertion that
    // Client shoule connect to Server before being closed.
    @Test(expected = AssertionError.class)
    public void testCloseBeforeConnectServer() {
        Client client = new Client();
        client.close();
    }
}
