package org.dync.ijkplayerlib.widget.receiver;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.util.ArrayMap;

/**
 * Created by KathLine on 2017/10/17.
 */

public class NetWorkControl {
    private static ArrayMap<String, NetworkChangedReceiver> receiverArrayMap = new ArrayMap<>();

    public interface NetWorkChangeListener {
        boolean isConnected(boolean wifiConnected, boolean wifiAvailable, boolean mobileConnected, boolean mobileAvailable);
    }

    public static NetworkChangedReceiver register(String tag, Activity activity) {
        NetworkChangedReceiver netWorkStateReceiver = new NetworkChangedReceiver();
        receiverArrayMap.put(tag, netWorkStateReceiver);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(netWorkStateReceiver, filter);
        return netWorkStateReceiver;
    }

    public static void unRegister(String tag, Activity activity) {
        if (receiverArrayMap.containsKey(tag)) {
            NetworkChangedReceiver receiver = receiverArrayMap.get(tag);
            activity.unregisterReceiver(receiver);
        }
    }
}
