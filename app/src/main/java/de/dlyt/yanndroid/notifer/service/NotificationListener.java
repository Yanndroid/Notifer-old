package de.dlyt.yanndroid.notifer.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class NotificationListener extends NotificationListenerService {

    public static HashMap<String, Integer> enabled_packages;
    private static boolean enabled;
    private static WebSocket webSocket;

    public static void setEnabled(boolean enabled) {
        NotificationListener.enabled = enabled;
        if (!enabled) webSocket.disconnect();
    }

    public static void setAddress(String ip, String port) {
        webSocket.disconnect();
        webSocket = new WebSocket(ip, port);
    }

    public static void testWS() {
        webSocket.send("test message from Notifer");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE);
        enabled = sharedPreferences.getBoolean("service_enabled", false);
        enabled_packages = new Gson().fromJson(sharedPreferences.getString("enabled_packages", "{\"de.dlyt.yanndroid.notifer\":-15167360}"), new TypeToken<HashMap<String, Integer>>() {
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
        if (!enabled || !enabled_packages.containsKey(sbn.getPackageName())) return;

        webSocket.send(createMessage(sbn, false));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (!enabled || !enabled_packages.containsKey(sbn.getPackageName())) return;

        webSocket.send(createMessage(sbn, true));
    }

    private HashMap<String, Object> createMessage(StatusBarNotification sbn, boolean removed) {
        HashMap<String, Object> output = new HashMap<>();
        Bundle bundle = sbn.getNotification().extras;

        output.put("package", sbn.getPackageName());
        output.put("id", sbn.getId());
        output.put("removed", removed);

        output.put("title", bundle.getString("android.title"));
        output.put("text", bundle.getString("android.text"));
        output.put("sub_text", bundle.getString("android.subText"));
        output.put("title_big", bundle.getString("android.title.big"));
        output.put("text_big", bundle.getString("android.bigText"));

        output.put("progress_indeterminate", bundle.getString("android.progressIndeterminate"));
        output.put("progress_max", bundle.getString("android.progressMax"));
        output.put("progress", bundle.getString("android.progress"));

        output.put("color", String.format("#%06X", (0xFFFFFF & enabled_packages.get(sbn.getPackageName()))));

        return output;
    }

}
