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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.phonemap.phonemap.wrapper.MessengerSender;

import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;
import static com.phonemap.phonemap.constants.API.ON_START;
import static com.phonemap.phonemap.constants.API.READY;
import static com.phonemap.phonemap.constants.API.RETURN;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Other.FILE_PREFIX;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.EXCEPTION;
import static com.phonemap.phonemap.constants.Sockets.FAILED_EXECUTING_CODE;
import static com.phonemap.phonemap.constants.Sockets.PATH;
import static com.phonemap.phonemap.constants.Sockets.RETURN_DATA_AND_CODE;
import static com.phonemap.phonemap.constants.Sockets.RETURN_RESULTS;

public class JSRunner extends Service {
    private static String LOG_TAG = "JSRunner";

    private String data;
    private MicroService service;
    private Messenger messenger = null;
    private Messenger response = new Messenger(new MessageHandler());


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RETURN_DATA_AND_CODE:
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(shutdownReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);

        Intent intent = new Intent(JSRUNNER_STOP_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        unregisterReceiver(shutdownReceiver);
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messenger = new Messenger(service);
            new MessengerSender(RETURN_DATA_AND_CODE).replyTo(response).send(messenger);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOG_TAG, "ConnectionManager stopped unexpectedly.");
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        service = new MicroService(
                getApplicationContext(),
                convertPathToURI(path),
                startListener,
                errorListener,
                exitListener
        );

        service.start();

        Intent intent = new Intent(JSRUNNER_STARTED_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                service.emit(ON_DESTROY, true);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                service.emit(ON_DESTROY, false);
            }
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
            Bundle bundle = new Bundle();
            bundle.putString(RETURN, payload.toString());

            new MessengerSender(RETURN_RESULTS).setData(bundle).send(messenger);

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

            Bundle bundle = new Bundle();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            bundle.putString(EXCEPTION, sw.toString());

            new MessengerSender(FAILED_EXECUTING_CODE).setData(bundle).send(messenger);
        }
    };

    private final MicroService.ServiceExitListener exitListener = new MicroService.ServiceExitListener() {
        @Override
        public void onExit(MicroService service, Integer exitCode) {
            Log.i(LOG_TAG, "Exiting execution");
        }
    };

    URI convertPathToURI(String path) throws URISyntaxException {
        return new URI(FILE_PREFIX + path);
    }
}
