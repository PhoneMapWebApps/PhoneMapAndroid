package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.phonemap.phonemap.constants.Server.WS_URL;
import static com.phonemap.phonemap.constants.Sockets.CODE;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.GET_CODE;
import static com.phonemap.phonemap.constants.Sockets.ID;
import static com.phonemap.phonemap.constants.Sockets.SET_CODE;
import static com.phonemap.phonemap.constants.Sockets.SET_ID;

public class Controller extends Service {
    private static String LOG_TAG = "SOCKET_IO";
    private int id;
    private Socket socket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            socket = IO.socket(WS_URL);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Invalid URI");
            stopSelf();
            return START_NOT_STICKY;
        }

        socket.on(Socket.EVENT_CONNECT, connectListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectListenerListener);
        socket.on(Socket.EVENT_ERROR, errorListener);

        socket.on(SET_ID, setIdListener);
        socket.on(SET_CODE, setCodeListener);

        socket.connect();

        startService(new Intent(getApplicationContext(), JSRunner.class));
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Connected");
        }
    };

    private final Emitter.Listener disconnectListenerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Disconnected");
        }
    };

    private final Emitter.Listener errorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Socket error occurred:");
            printArgs(args);
        }
    };

    private final Emitter.Listener setIdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[1];
                id = message.getInt(ID);
                socket.emit(GET_CODE);
            } catch (JSONException e) {
                exitOnBadArgs(SET_ID, args);
            }
        }
    };

    private final Emitter.Listener setCodeListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[0];
                //ToDo: Save code, start service and pass data to it
                message.getString(CODE);
                message.getString(DATA);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void exitOnBadArgs(String event, Object... args) {
        Log.e(LOG_TAG, "Malformed args for event:" + event);
        printArgs(args);
        stopSelf();
    }

    private void printArgs(Object... args) {
        for (Object arg : args) {
            Log.e(LOG_TAG, String.valueOf(arg));
        }
    }
}
