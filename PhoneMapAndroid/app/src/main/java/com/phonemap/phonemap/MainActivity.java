package com.phonemap.phonemap;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final MicroService.EventListener readyListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, JSONObject payload) {
                service.emit("ping");
                Log.i("LOG", "Ready");
            }
        };

        final MicroService.EventListener pongListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, final JSONObject payload) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("LOG", "Received Response");
                    }
                });
            }
        };

        final MicroService.ServiceStartListener startListener = new MicroService.ServiceStartListener() {
            @Override
            public void onStart(MicroService service) {
                service.addEventListener("ready", readyListener);
                service.addEventListener("pong", pongListener);
            }
        };

        try {
            MicroService service = new MicroService(
                    MainActivity.this,
                    new URI("android.resource://com.phonemap.phonemap/raw/" + R.raw.test),
                    startListener
            );
            service.start();
            Log.i("LOG", "Started");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        };
    }
}
