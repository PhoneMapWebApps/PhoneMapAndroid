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

public class Controller extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService(new Intent(getApplicationContext(), JSRunner.class));
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
