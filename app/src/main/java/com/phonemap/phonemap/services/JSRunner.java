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

import org.json.JSONObject;
import org.liquidplayer.service.MicroService;
import org.liquidplayer.service.MicroService.EventListener;
import org.liquidplayer.service.MicroService.ServiceErrorListener;
import org.liquidplayer.service.MicroService.ServiceExitListener;
import org.liquidplayer.service.MicroService.ServiceStartListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static com.phonemap.phonemap.constants.API.ON_START;
import static com.phonemap.phonemap.constants.API.READY;
import static com.phonemap.phonemap.constants.API.RETURN;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_FAILED_EXECUTION;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Other.FILE_PREFIX;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Sockets.COMPLETED_SUBTASK;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.EXCEPTION;
import static com.phonemap.phonemap.constants.Sockets.FAILED_EXECUTING_CODE;
import static com.phonemap.phonemap.constants.Sockets.NEW_SUBTASK;
import static com.phonemap.phonemap.constants.Sockets.PATH;

public class JSRunner extends Service {
    private static final String LOG_TAG = "JSRunner";

    private final ServiceExitListener exitListener = new ServiceExitListener() {
        @Override
        public void onExit(MicroService service, Integer exitCode) {
            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(JSRUNNER_STOP_INTENT));
        }
    };

    private final EventListener readyListener = new EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            service.emit(ON_START, data);
        }
    };

    private final ServiceStartListener startListener = new ServiceStartListener() {
        @Override
        public void onStart(MicroService service) {
            service.addEventListener(READY, readyListener);
            service.addEventListener(RETURN, returnListener);
        }
    };

    private String data;
    private MicroService service;
    private MessengerSender messengerSender;
    private ShutdownReceiver shutdownReceiver;

    private Messenger incomingMessageHandler = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEW_SUBTASK:
                    try {
                        startMicroService(msg.getData());
                    } catch (URISyntaxException e) {
                        // Todo: Error handling - Tell server that something went wrong.
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Invalid URI, could not start MicroService");
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    private final EventListener returnListener = new EventListener() {
        @Override
        public void onEvent(MicroService service, String event, JSONObject payload) {
            Bundle bundle = new Bundle();
            bundle.putString(RETURN, payload.toString());

            messengerSender.setMessage(COMPLETED_SUBTASK).setData(bundle).send();

            service.getProcess().exit(0);

            getDataAndCode();
        }
    };

    private final ServiceErrorListener errorListener = new ServiceErrorListener() {
        @Override
        public void onError(MicroService service, Exception e) {
            Log.e(LOG_TAG, "Error occurred within MicroService");

            Bundle bundle = new Bundle();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            bundle.putString(EXCEPTION, sw.toString());

            messengerSender.setMessage(FAILED_EXECUTING_CODE).setData(bundle).send();
            getDataAndCode();

            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(JSRUNNER_FAILED_EXECUTION));
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messengerSender = new MessengerSender(new Messenger(service));
            getDataAndCode();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOG_TAG, "SocketConnectionManager stopped unexpectedly.");
            stopSelf();
        }
    };

    public JSRunner() {
    }

    public JSRunner(MicroService service) {
        this.service = service;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindService(new Intent(this, SocketConnectionManager.class), connection, Context.BIND_AUTO_CREATE);

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

    private void startMicroService(Bundle bundle) throws URISyntaxException {
        String path = bundle.getString(PATH);
        String task_name = bundle.getString(TASK_NAME);

        data = bundle.getString(DATA);

        service = new MicroService(
                getApplicationContext(),
                convertPathToURI(path),
                startListener,
                errorListener,
                exitListener
        );

        service.start();
        shutdownReceiver = new ShutdownReceiver(service);

        Intent intent = new Intent(JSRUNNER_STARTED_INTENT);
        intent.putExtra(TASK_NAME, task_name);

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private void getDataAndCode() {
        messengerSender.setMessage(NEW_SUBTASK).sendRepliesTo(incomingMessageHandler).send();
    }

    URI convertPathToURI(String path) throws URISyntaxException {
        return new URI(FILE_PREFIX + path);
    }
}
