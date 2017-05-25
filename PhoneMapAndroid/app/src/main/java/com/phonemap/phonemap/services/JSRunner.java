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
import com.phonemap.phonemap.wrappers.IntentFilterBuilder;

import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;

public class JSRunner extends Service {
    private MicroService service;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilterBuilder()
                .withAction(Intent.ACTION_SHUTDOWN)
                .withAction(Intent.ACTION_SCREEN_ON)
                .withAction(Intent.ACTION_POWER_DISCONNECTED).build();

        registerReceiver(shutdownReceiver, filter);

        try {
            startMicroService();
        } catch (URISyntaxException e) {
            System.err.println("Failed to start service! Telling server to put job somewhere else...");
            // Handle error by reporting back to the server that there is a problem with this phone and to try another one.
            e.printStackTrace();
        }

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMicroService() throws URISyntaxException {
        service = new MicroService(
                getApplicationContext(),
                new URI("android.resource://com.phonemap.phonemap/raw/" + R.raw.test),
                startListener,
                errorListener,
                exitListener
        );
        service.start();
    }

    private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            service.emit("onShutdown");
        }
    };

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
            service.getProcess().exit(0);
        }
    };

    private final MicroService.ServiceStartListener startListener = new MicroService.ServiceStartListener() {
        @Override
        public void onStart(MicroService service) {
            service.addEventListener("ready", readyListener);
            service.addEventListener("return", returnListener);
        }
    };

    private final MicroService.ServiceErrorListener errorListener = new MicroService.ServiceErrorListener() {
        @Override
        public void onError(MicroService service, Exception e) {
            // Handle errors
        }
    };

    private final MicroService.ServiceExitListener exitListener = new MicroService.ServiceExitListener() {
        @Override
        public void onExit(MicroService service, Integer exitCode) {
            // Handle finish of execution
        }
    };
}
