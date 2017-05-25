package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.phonemap.phonemap.wrappers.HttpURLConnectionBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Controller extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WebserverListener webserverListener = new WebserverListener();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("http://146.169.45.121:5000").build();

        client.newWebSocket(request, webserverListener);

        startService(new Intent(getApplicationContext(), JSRunner.class));
        client.dispatcher().executorService().shutdown();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void downloadFile(String url, String outputFileName) throws IOException {
        HttpURLConnection urlConnection = new HttpURLConnectionBuilder(url).setRequestMethod("GET").setDoOutput(true).connect();

        File outputFile = new File(getApplicationContext().getFilesDir(), outputFileName);
        FileOutputStream fileOutput = new FileOutputStream(outputFile);
        InputStream inputStream = urlConnection.getInputStream();

        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        fileOutput.close();
    }
}
