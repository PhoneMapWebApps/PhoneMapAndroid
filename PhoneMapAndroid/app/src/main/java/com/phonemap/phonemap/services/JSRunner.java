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

import static com.phonemap.phonemap.constants.API.ON_DESTROY;
import static com.phonemap.phonemap.constants.API.ON_START;
import static com.phonemap.phonemap.constants.API.READY;
import static com.phonemap.phonemap.constants.API.RETURN;
import static com.phonemap.phonemap.constants.Other.FILE_PREFIX;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.PATH;

public class JSRunner extends Service {
    private MicroService service;
    private static String LOG_TAG = "JSRunner";

    public String data;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(shutdownReceiver, filter);

        String path = intent.getStringExtra(PATH);
        this.data = intent.getStringExtra(DATA);

        try {
            startMicroService(path);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Failed to start service! Telling server to put job somewhere else...");
            // ToDo: Handle error by reporting back to the server that there is a problem with this phone and to try another one.
            e.printStackTrace();
        }

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMicroService(String path) throws URISyntaxException {
        // Test file: "android.resource://com.phonemap.phonemap/raw/" + R.raw.test

        service = new MicroService(
                getApplicationContext(),
                convertPathToURI(path),
                startListener,
                errorListener,
                exitListener
        );
        service.start();
    }

    private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            service.emit(ON_DESTROY);
        }
    };

    private final MicroService.EventListener readyListener = new MicroService.EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            service.emit(ON_START, data);
        }
    };

    private final MicroService.EventListener returnListener = new MicroService.EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            Log.i(LOG_TAG, payload.toString());
            service.getProcess().exit(0);
        }
    };

    private final MicroService.ServiceStartListener startListener = new MicroService.ServiceStartListener() {
        @Override
        public void onStart(MicroService service) {
            service.addEventListener(READY, readyListener);
            service.addEventListener(RETURN, returnListener);
        }
    };

    private final MicroService.ServiceErrorListener errorListener = new MicroService.ServiceErrorListener() {
        @Override
        public void onError(MicroService service, Exception e) {
            Log.e(LOG_TAG, "Error occurred within MicroService");
            Log.e(LOG_TAG, String.valueOf(e));
        }
    };

    private final MicroService.ServiceExitListener exitListener = new MicroService.ServiceExitListener() {
        @Override
        public void onExit(MicroService service, Integer exitCode) {
            Log.i(LOG_TAG, "Exiting execution");
        }
    };

    private URI convertPathToURI(String path) throws URISyntaxException {
        return new URI(FILE_PREFIX + path);
    }
}
