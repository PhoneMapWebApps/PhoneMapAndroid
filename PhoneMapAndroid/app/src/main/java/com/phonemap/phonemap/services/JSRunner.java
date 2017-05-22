package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.phonemap.phonemap.R;

import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;


public class JSRunner extends Service {
    private MicroService service;

    private final MicroService.EventListener readyListener = new MicroService.EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            service.emit("onStart");
        }
    };

    private final MicroService.EventListener returnListener = new MicroService.EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            Log.i("LOG", payload.toString());
        }
    };

    private final MicroService.ServiceStartListener mainListener = new MicroService.ServiceStartListener() {
        @Override
        public void onStart(MicroService service) {
            service.addEventListener("ready", readyListener);
            service.addEventListener("return", returnListener);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(shutdownReceiver, filter);

        try {
            service = new MicroService(
                    getApplicationContext(),
                    new URI("android.resource://com.phonemap.phonemap/raw/" + R.raw.test),
                    mainListener
            );
            service.start();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return Service.START_STICKY;
    }

    BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            service.emit("onShutdown");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
