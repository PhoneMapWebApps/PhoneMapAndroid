package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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
    private static String LOG_TAG = "JSRunner";

    private String data;
    private MicroService service;
    private Messenger messenger = null;
    private Messenger response = new Messenger(new MessageHandler());
    private boolean bound;


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    startMicroService(msg.getData());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindService(new Intent(this, ConnectionManager.class), connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messenger = new Messenger(service);
            bound = true;

            Message msg = Message.obtain(null, 0);
            msg.replyTo = response;

            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            //ToDo: Handle disconnect
            service = null;
            bound = false;
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(shutdownReceiver, filter);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMicroService(Bundle bundle) {
        data = bundle.getString(DATA);

        try {
            startMicroService(bundle.getString(PATH));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid URI, could not start MicroService");
        }
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
