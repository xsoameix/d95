package client;

import java.util.TimerTask;

public class Logger extends TimerTask {
    private Store store;

    public Logger(Store store) {
        this.store = store;
    }

    public void run() {
        store.showTreasures();
    }
}
