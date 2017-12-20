package org.nestordeveloper.controlwifiiot.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class WifiContent {

    /**
     * An array of sample (Wifi) items.
     */
    public static final List<WifiItem> ITEMS = new ArrayList<WifiItem>();

    /**
     * A map of sample (Wifi) items, by ID.
     */
    public static final Map<String, WifiItem> ITEM_MAP = new HashMap<String, WifiItem>();

    private static final int COUNT = 25;

    static {
        // Código que se ejecuta al principio de todo
    }

    private static void addItem(WifiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static WifiItem createWifiItem(int position) {
        return new WifiItem(String.valueOf(position), "Item " + position);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A Wifi item representing a piece of content.
     */
    public static class WifiItem {
        public final String id;
        public final String nombre;


        public WifiItem(String id, String content) {
            this.id = id;
            this.nombre = content;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
