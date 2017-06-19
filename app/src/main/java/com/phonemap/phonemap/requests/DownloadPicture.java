package com.phonemap.phonemap.requests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPicture extends AsyncTask<String, Void, Bitmap> {

    public static final String LOG_TAG = "DownloadString";

    private AsyncBitmapDownloadListener callback;

    public DownloadPicture(AsyncBitmapDownloadListener caller){
        callback = caller;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to download picture");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        callback.onBitmapDownloaded(bitmap);
    }
}
