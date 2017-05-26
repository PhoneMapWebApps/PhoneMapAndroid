package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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

import static com.phonemap.phonemap.constants.Server.WS_URL;

public class Controller extends Service {
    private static String LOG_TAG = "SOCKET_IO";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Socket socket;
        try {
            socket = IO.socket(WS_URL);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Invalid URI");
            stopSelf();
            return START_NOT_STICKY;
        }

        socket.on(Socket.EVENT_CONNECT, connect);
        socket.on(Socket.EVENT_DISCONNECT, disconnect);
        socket.on(Socket.EVENT_MESSAGE, message);
        socket.on(Socket.EVENT_ERROR, error);

        socket.connect();

        startService(new Intent(getApplicationContext(), JSRunner.class));
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Emitter.Listener connect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Connected");
        }
    };

    private final Emitter.Listener disconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Disconnected");
        }
    };

    private final Emitter.Listener message = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, String.valueOf(args.length));
        }
    };

    private final Emitter.Listener error = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "We fucked up");
            Log.i(LOG_TAG, String.valueOf(args.length));
            Log.i(LOG_TAG, String.valueOf(args[0]));
        }
    };

    private void downloadFile(String urlPath, String filename) {
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

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }

            fileOutput.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
