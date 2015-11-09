package server;

import java.util.ArrayList;
import java.util.Hashtable;

public class Store {
    private ArrayList<String> keys;
    private Hashtable<String, Integer> treasure;

    public Store() {
        keys = new ArrayList<String>();
        treasure = new Hashtable<String, Integer>();
        String names[] = {"A", "B", "C"};
        for (String name : names) {
            keys.add(name);
            treasure.put(name, 0);
        }
    }

    public Hashtable<String, Integer> getTreasure() {
        return treasure;
    }

    // If any client join or leave, this method will be executed:
    //   The client's treasure are auto deleted after 5 seconds,
    //   so all treasures are restored to server.
    //   It is fair to every client.
    public void releaseAllTreasures() {
        synchronized(treasure) {
            for (String key : keys) {
                treasure.put(key, 0);
            }
        }
    }

    public void showTreasures() {
        for (String key : keys) {
            Integer clientId = treasure.get(key);
            String owned = clientId > 0 ? "YES" : "NO";
            System.out.printf("%s %-3s %d\n", key, owned, clientId);
        }
    }
}
