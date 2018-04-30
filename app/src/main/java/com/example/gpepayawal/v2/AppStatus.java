package com.example.gpepayawal.v2;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class AppStatus {

    private static AppStatus instance = new AppStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static AppStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            //if(connected) Log.i("connection", "connected to network");
            //else Log.i("connection", "not connected to network");
            return connected;
        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            //Log.v("connectivity", e.toString());
        }
        return connected;
    }
}
