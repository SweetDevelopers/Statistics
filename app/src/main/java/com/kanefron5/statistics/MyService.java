package com.kanefron5.statistics;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private BroadcastReceiver receiver;
//TODO:очистить лог

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);




        if (!getSystemProperty("ro.build.newelos").equals(prefs.getString("build_id", null))) {
            if(hasConnection(this)) {
                Log.d(TAG,"isConnected");
                send(prefs);
            }else{
                Log.d(TAG,"isn't connected");
                IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        send(prefs);
                    }
                };
                registerReceiver(receiver, filter);

            }
        } else onDestroy();


       // Log.d(TAG, "http://newelos.aliceteam.xyz/statistics.php?device=" + Build.DEVICE + "&brand=" + Build.BRAND + "&model=" + Build.MODEL + "&build_id=" + Build.DISPLAY);
    }

    public static boolean hasConnection(Context c) {
        if (c == null)
            return false;

        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isAvailable() &&
                cm.getActiveNetworkInfo().isConnected());
    }
    public String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
    public void send(final SharedPreferences prefs){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        final String imei = telephonyManager.getDeviceId();
        Log.d(TAG, imei);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URLConnection connection = new URL("http://newelos.aliceteam.xyz/statistics.php?device=" + Build.DEVICE
                            + "&brand=" + Build.BRAND
                            + "&model=" + Build.MODEL
                            + "&build_id=" + getSystemProperty("ro.build.newelos")
                            + "&imei=" + imei).openConnection();

                    InputStream is = connection.getInputStream();
                    is.close();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("build_id", getSystemProperty("ro.build.newelos"));
                    editor.commit();
                    if (receiver != null) {
                        unregisterReceiver(receiver);
                        receiver = null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        }).start();
    }
}
