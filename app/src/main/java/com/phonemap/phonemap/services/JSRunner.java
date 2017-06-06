package com.phonemap.phonemap.services;

import android.app.Service;
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
    private MessengerSender messengerSender;
    private Messenger response = new Messenger(new MessageHandler());
    private ShutdownReceiver shutdownReceiver;

    public JSRunner() {
    }

    public JSRunner(MicroService service) {
        this.service = service;
    }

    private class MessageHandler extends Handler {
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

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messengerSender = new MessengerSender(new Messenger(service));
            getDataAndCode();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOG_TAG, "ConnectionManager stopped unexpectedly.");
            stopSelf();
        }
    };

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
        shutdownReceiver = new ShutdownReceiver(service);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(JSRUNNER_STARTED_INTENT));
    }

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

            messengerSender.setMessage(RETURN_RESULTS).setData(bundle).send();

            service.getProcess().exit(0);

            getDataAndCode();
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

            messengerSender.setMessage(FAILED_EXECUTING_CODE).setData(bundle).send();
            getDataAndCode();
        }
    };

    private final MicroService.ServiceExitListener exitListener = new MicroService.ServiceExitListener() {
        @Override
        public void onExit(MicroService service, Integer exitCode) {
            Log.i(LOG_TAG, "Exiting execution");
        }
    };

    private void getDataAndCode() {
        messengerSender.setMessage(RETURN_DATA_AND_CODE).sendRepliesTo(response).send();
    }

    URI convertPathToURI(String path) throws URISyntaxException {
        return new URI(FILE_PREFIX + path);
    }
}
