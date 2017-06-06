package com.phonemap.phonemap.requests;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadString extends AsyncTask<String, Void, String> {

    public static final String LOG_TAG = "DownloadString";

    private AsyncTaskListener callback;

    public DownloadString(AsyncTaskListener caller){
        callback = caller;
    }

    @Override
    protected String doInBackground(String... params) {
        Request request = new Request.Builder().url(params[0]).build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to download data");
        }

        return null;
    }

    @Override
    protected void onPostExecute(String str){
        callback.onStringDownloaded(str);
    }
}