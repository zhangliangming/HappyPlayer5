package org.dync.ijkplayerlib.widget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 网络改变监控广播
 * <p>
 * 监听网络的改变状态,只有在用户操作网络连接开关(wifi,mobile)的时候接受广播
 * <p>
 * <p>
 */
public class NetworkChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangedReceiver";

    private NetWorkControl.NetWorkChangeListener netWorkChangeListener;

    public void setNetWorkChangeListener(NetWorkControl.NetWorkChangeListener listener) {
        netWorkChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        networkListener(context, intent);
        onlyWifiNetworkListener(intent);
    }

    private void networkListener(Context context, Intent intent) {
        boolean isWifiConnected = false, isMobileConnected = false, isWifiAvailable = false, isMobileAvailable = false;
        // 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
        // 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
        // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) { // connected to the internet
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        isWifiConnected = true;
                        if (info.isAvailable()) {
                            Log.d(TAG, "当前WiFi连接可用 ");
                            isWifiAvailable = true;
                        } else {
                            Log.d(TAG, "当前WiFi连接不可用 ");
                            isWifiAvailable = false;
                        }
                    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        isMobileConnected = true;
                        if (info.isAvailable()) {
                            Log.d(TAG, "当前移动网络连接可用 ");
                            isMobileAvailable = true;
                        } else {
                            Log.d(TAG, "当前移动网络连接不可用 ");
                            isMobileAvailable = false;
                        }
                    }
                } else {//
                    Log.d(TAG, "当前没有网络连接，请确保你已经打开网络!! ");
                    isWifiConnected = false;
                    isWifiAvailable = false;
                    isMobileConnected = false;
                    isMobileAvailable = false;
                }

                Log.d(TAG, "info.getTypeName()" + info.getTypeName());
                Log.d(TAG, "getSubtypeName()" + info.getSubtypeName());
                Log.d(TAG, "getState()" + info.getState());
                Log.d(TAG, "getDetailedState()"
                        + info.getDetailedState().name());
                Log.d(TAG, "getDetailedState()" + info.getExtraInfo());
                Log.d(TAG, "getType()" + info.getType());
            } else {   // not connected to the internet
                Log.d(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                isWifiConnected = false;
                isWifiAvailable = false;
                isMobileConnected = false;
                isMobileAvailable = false;
            }

            if (netWorkChangeListener != null) {
                netWorkChangeListener.isConnected(isWifiConnected, isWifiAvailable, isMobileConnected, isMobileAvailable);
            }
        }
    }

    /**
     * 仅接收WiFi通知
     *
     * @param intent
     */
    private void onlyWifiNetworkListener(Intent intent) {
        boolean isWifiConnected = false, isMobileConnected = false, isWifiAvailable = false, isMobileAvailable = false;
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//            ConnectivityManager manager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        isWifiConnected = true;
                        if(info.isAvailable()){
                            Log.i(TAG, "WIFI网络连上");
                            isWifiAvailable = true;
                        }else {
                            Log.i(TAG, "WIFI网络连上，但不可用");
                            isWifiAvailable = false;
                        }
                    }else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
                        isMobileConnected = true;
                        if(info.isAvailable()){
                            Log.i(TAG, "手机网络数据连上");
                            isMobileAvailable = true;
                        }else {
                            Log.i(TAG, "手机网络数据连上，但不可用");
                            isMobileAvailable = false;
                        }
                    }
                } else {
                    switch (info.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                            Log.i(TAG, "WIFI网络断开");
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                            Log.i(TAG, "手机网络数据断开");
                            break;
                    }
                    if(info.isConnected()) {

                    }else {

                    }
                }
            }
            Log.i(TAG, "NetworkInfo: " + info.toString());
//            if (info != null) { // connected to the internet
//                if (NetworkInfo.State.CONNECTED == info.getState()) {
//                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
//                        // connected to wifi
//                        isWifiConnected = true;
//                        if (info.isAvailable()) {
//                            Log.d(TAG, "当前WiFi连接可用 ");
//                            isWifiAvailable = true;
//                        } else {
//                            Log.d(TAG, "当前WiFi连接不可用 ");
//                            isWifiAvailable = false;
//                        }
//                    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
//                        // connected to the mobile provider's data plan
//                        isMobileConnected = true;
//                        if (info.isAvailable()) {
//                            Log.d(TAG, "当前移动网络连接可用 ");
//                            isMobileAvailable = true;
//                        } else {
//                            Log.d(TAG, "当前移动网络连接不可用 ");
//                            isMobileAvailable = false;
//                        }
//                    }
//                } else {//
//                    Log.d(TAG, "当前没有网络连接，请确保你已经打开网络!! ");
//                    isWifiConnected = false;
//                    isWifiAvailable = false;
//                    isMobileConnected = false;
//                    isMobileAvailable = false;
//                }
//            }
        }
        if (netWorkChangeListener != null) {
            netWorkChangeListener.isConnected(isWifiConnected, isWifiAvailable, isMobileConnected, isMobileAvailable);
        }
    }

    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "手机网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }
}
