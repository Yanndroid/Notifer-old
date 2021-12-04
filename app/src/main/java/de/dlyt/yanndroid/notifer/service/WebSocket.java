package de.dlyt.yanndroid.notifer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;

public class WebSocket {

    private okhttp3.WebSocket mWebSocket;
    private boolean connected;
    private String ip, port;

    public WebSocket(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        if (!validAddress()) return;

        mWebSocket = new OkHttpClient().newWebSocket(new Request.Builder().url("ws://" + ip + ":" + port).build(), new WebSocketListener() {
            @Override
            public void onFailure(@NonNull okhttp3.WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                connected = false;
            }

            @Override
            public void onOpen(@NonNull okhttp3.WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                connected = true;
            }
        });
    }

    private boolean validAddress() {
        return ip != null && port != null; //todo
    }

    public void disconnect() {
        connected = false;
        if (mWebSocket != null) mWebSocket.close(1000, "service stopped");
    }

    public void send(String message) {
        if (!connected) connect();
        if (mWebSocket != null) mWebSocket.send(message);
    }

    public void send(HashMap<String, Object> hashMap) {
        send(new Gson().toJson(hashMap));
    }
}
