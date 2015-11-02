package client;

import java.util.ArrayList;
import java.util.Hashtable;

public class Store {
    private ArrayList<String> keys;
    private Hashtable<String, Integer> treasure;

    public Store() {
        keys = new ArrayList<String>();
        treasure = new Hashtable<String, Integer>();
        String names[] = {"A", "B", "C"};
        for (String name : names) keys.add(name);
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public Hashtable<String, Integer> getTreasure() {
        return treasure;
    }

    public void showTreasures() {
        for (String key : keys) {
            synchronized(treasure) {
                if (treasure.containsKey(key)) {
                    System.out.printf("%s %-3s %d\n", key, "YES",
                            treasure.get(key));
                } else {
                    System.out.printf("%s %-3s\n", key, "NO");
                }
            }
        }
    }
}
