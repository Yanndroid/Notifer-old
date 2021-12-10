package de.dlyt.yanndroid.notifer.service;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;

public class NotificationListener extends NotificationListenerService {

    public static HashMap<String, Integer> enabled_packages;
    private static boolean enabled;
    private static WebSocket webSocket;
    private SharedPreferences sharedPreferences;

    public static void setEnabled(boolean enabled) {
        NotificationListener.enabled = enabled;
        if (!enabled) webSocket.disconnect();
    }

    public static void setAddress(String ip, String port) {
        webSocket.disconnect();
        webSocket = new WebSocket(ip, port);
    }

    public static void sendToWS(HashMap<String, Object> hashMap) {
        webSocket.send(hashMap);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE);
        enabled = sharedPreferences.getBoolean("service_enabled", false);
        enabled_packages = new Gson().fromJson(sharedPreferences.getString("enabled_packages", "{}"), new TypeToken<HashMap<String, Integer>>() {
        }.getType());

        webSocket = new WebSocket(sharedPreferences.getString("ws_ip", null), sharedPreferences.getString("ws_port", null));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webSocket.disconnect();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (!enabled || !hasConnection() || !enabled_packages.containsKey(sbn.getPackageName()))
            return;

        webSocket.send(createMessage(sbn, false));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (!enabled || !hasConnection() || !enabled_packages.containsKey(sbn.getPackageName()))
            return;

        webSocket.send(createMessage(sbn, true));
    }

    private boolean hasConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private HashMap<String, Object> createMessage(StatusBarNotification sbn, boolean removed) {
        HashMap<String, Object> output = new HashMap<>();
        Bundle bundle = sbn.getNotification().extras;

        try {
            PackageManager pm = getPackageManager();
            output.put("label", pm.getApplicationLabel(pm.getApplicationInfo(sbn.getPackageName(), 0)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        output.put("package", sbn.getPackageName());
        output.put("id", sbn.getId());
        output.put("time", sbn.getPostTime());
        output.put("ongoing", sbn.isOngoing());
        output.put("template", sbn.getNotification().extras.getString(Notification.EXTRA_TEMPLATE));
        output.put("removed", removed);

        output.put("title", bundle.getString("android.title"));
        output.put("text", bundle.getString("android.text"));
        output.put("sub_text", bundle.getString("android.subText"));
        output.put("title_big", bundle.getString("android.title.big"));
        output.put("text_big", bundle.getString("android.bigText"));

        output.put("progress_indeterminate", bundle.getBoolean("android.progressIndeterminate"));
        output.put("progress_max", bundle.getInt("android.progressMax"));
        output.put("progress", bundle.getInt("android.progress"));

        switch (sharedPreferences.getString("color_format", "hex")) {
            default:
            case "hex":
                output.put("color", String.format("#%06X", (0xFFFFFF & enabled_packages.get(sbn.getPackageName()))));
                break;
            case "rgb":
                Color color = Color.valueOf(enabled_packages.get(sbn.getPackageName()));
                output.put("color", Arrays.toString(new int[]{(int) (color.red() * 255), (int) (color.green() * 255), (int) (color.blue() * 255)}));
                break;
            case "hsv":
                float[] hsv_color = new float[3];
                Color.colorToHSV(enabled_packages.get(sbn.getPackageName()), hsv_color);
                output.put("color", Arrays.toString(hsv_color));
                break;
            case "int":
                output.put("color", enabled_packages.get(sbn.getPackageName()));
                break;
        }

        return output;
    }

}
