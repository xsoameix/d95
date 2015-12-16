package server;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import static client.Client.*;

import static suite.TestSuite.*;

import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestServer {

    // Test initTCPServer() method.
    @Test
    public void testInitTCPServer() {
        try {
            Store store = new Store();
            Server server = new Server(store);
            server.initTCPServer();
            Thread.sleep(SETUP_TIME);
            Socket socket = new Socket("127.0.0.1", 5000);
            socket.close();
            server.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test getClientIPTable() method.
    @Test
    public void testGetClientIPTable() {
        try {
            Store store = new Store();
            Server server = new Server(store);
            server.initTCPServer();
            Thread.sleep(SETUP_TIME);
            ArrayList<Socket> socks = new ArrayList<Socket>();
            for (int i = 0; i < 20; i++) {
                Socket socket = new Socket("127.0.0.1", 5000);
                Thread.sleep(ACCEPT_TIME);
                socks.add(socket);
                Vector<InetAddress> iptable = server.getClientIPTable();
                for (InetAddress addr : iptable) {
                    assertEquals(addr.getHostAddress(), "127.0.0.1");
                }
                assertEquals(iptable.size(), socks.size());
            }
            Iterator<Socket> itor = socks.iterator();
            while (itor.hasNext()) {
                Socket socket = itor.next();
                socket.close();
                itor.remove();
                Thread.sleep(TRANSFER_TIME);
                Vector<InetAddress> iptable = server.getClientIPTable();
                assertEquals(iptable.size(), socks.size());
            }
            server.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test whether Server can handle movecode.
    @Test
    public void testHandlingMovecode() {
        try {
            int expected[] = {
                GET,
                TURNEAST,
                TURNSOUTH,
                TURNNORTH,
                TURNWEST
            };
            final Vector<Integer> actual = new Vector<Integer>();
            Store store = new Store() {
                public void getItem(int clientno) {
                    actual.add(GET);
                }
                public void updateDirection(int clientno, int movecode) {
                    actual.add(movecode);
                }
            };
            Server server = new Server(store);
            server.initTCPServer();
            Thread.sleep(SETUP_TIME);
            Socket socket = new Socket("127.0.0.1", 5000);
            DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
            for (Integer movecode : expected) {
                out.writeInt(movecode);
            }
            Thread.sleep(TRANSFER_TIME);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(actual.get(i).intValue(), expected[i]);
            }
            out.close();
            socket.close();
            server.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test whether Server can graceful shutdown when
    // the socket is connected.
    @Test
    public void testCloseServerBeforeSocketClosed() {
        try {
            Store store = new Store();
            Server server = new Server(store);
            server.initTCPServer();
            Thread.sleep(SETUP_TIME);
            Socket socket = new Socket("127.0.0.1", 5000);
            server.close();
            socket.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test Server a the assertion that
    // Server should be initialized before being closed.
    @Test(expected = AssertionError.class)
    public void testCloseServerBeforeServerInitialized() {
        try {
            Store store = new Store();
            Server server = new Server(store);
            server.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // Test Server a the assertion that
    // Server should be initialized before being closed.
    @Test(expected = AssertionError.class)
    public void testCloseServerTwice() {
        try {
            Store store = new Store();
            Server server = new Server(store);
            server.initTCPServer();
            Thread.sleep(SETUP_TIME);
            server.close();
            server.close();
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
