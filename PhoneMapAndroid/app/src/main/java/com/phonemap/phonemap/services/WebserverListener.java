package com.phonemap.phonemap.services;


import android.util.Log;

import com.phonemap.phonemap.wrappers.AndroidLog;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebserverListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(AndroidLog.INFO, "Opened websocket listener");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(AndroidLog.INFO, "Receiving : " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.i(AndroidLog.INFO, "Receiving bytes : " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.i(AndroidLog.INFO, "Closing : " + code + " / " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.i(AndroidLog.INFO, "Error : " + t.getMessage());
    }
}
