package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public static final int TURNEAST  = 0;
    public static final int TURNSOUTH = 1;
    public static final int TURNNORTH = 2;
    public static final int TURNWEST  = 3;
    public static final int GET       = 4;

    private static final int STATE_INITIAL = 0;
    private static final int STATE_CONNECTED = 1;

    private Socket socket;
    private DataOutputStream out;
    private int state = STATE_INITIAL;

    public boolean connectServer(InetAddress serverip) {
        try {
            assert state == STATE_INITIAL : "client should be initial";
            socket = new Socket(serverip, 5000);
            out = new DataOutputStream(socket.getOutputStream());
            state = STATE_CONNECTED;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void inputMove(int movecode) {
        try {
            assert state == STATE_CONNECTED : "client should be connected";
            out.writeInt(movecode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            assert state == STATE_CONNECTED : "client should be connected";
            out.close();
            socket.close();
            state = STATE_INITIAL;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
