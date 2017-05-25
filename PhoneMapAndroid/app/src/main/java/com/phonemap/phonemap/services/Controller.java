package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Controller extends Service {
    private static String LOG_TAG =  "SOCKET_IO";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            final Socket socket = IO.socket("http://146.169.45.121:5000/test");

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(LOG_TAG, "Connected");
                    JSONObject object = new JSONObject();
                    try {
                        object.put("data", "Sup");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("my_broadcast_event", object);
                    socket.emit("my_broadcast_event", object);
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(LOG_TAG, "Disconnected");
                }
            });

            socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(LOG_TAG, String.valueOf(args.length));
                }
            });

            socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(LOG_TAG, "We fucked up");
                    Log.i(LOG_TAG, String.valueOf(args.length));
                    Log.i(LOG_TAG, String.valueOf(args[0]));
                }
            });

            socket.on(Socket.EVENT_PING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(LOG_TAG, "Ping");
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        startService(new Intent(getApplicationContext(), JSRunner.class));
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void downloadFile(String urlPath, String filename){
        try {
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            File file = new File(getApplicationContext().getFilesDir(), filename);

            FileOutputStream fileOutput = new FileOutputStream(file);

            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
            }

            fileOutput.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
