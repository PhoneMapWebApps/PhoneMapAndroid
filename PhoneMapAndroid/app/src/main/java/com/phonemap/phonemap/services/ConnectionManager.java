package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import com.phonemap.phonemap.wrapper.MessengerSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.phonemap.phonemap.constants.Server.WS_URL;
import static com.phonemap.phonemap.constants.Sockets.*;
import static com.phonemap.phonemap.utils.Utils.bundleToJSON;

public class ConnectionManager extends Service {
    private static String LOG_TAG = "ConnectionManager";
    private int id;
    private Socket socket;

    private final Messenger messenger = new Messenger(new MessageHandler());
    private final BlockingQueue<Bundle> toProcess = new LinkedBlockingQueue<>();

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_AND_RETURN_DATA:
                    connectAndReturnData(msg.replyTo);
                    break;
                case RETURN_RESULTS:
                    socket.emit(SOCKET_RETURN, bundleToJSON(msg.getData()));
                    break;
                case FAILED_EXECUTING_CODE:
                    socket.emit(SOCKET_FAILED_EXECUTING, bundleToJSON(msg.getData()));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void connectAndReturnData(Messenger messenger) {
        try {
            socket = IO.socket(WS_URL);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Invalid URI");
            stopSelf();
        }

        socket.on(Socket.EVENT_CONNECT, connectListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectListenerListener);
        socket.on(Socket.EVENT_ERROR, errorListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener);

        socket.on(SOCKET_SET_ID, setIdListener);
        socket.on(SOCKET_SET_CODE, setCodeListener);

        socket.connect();

        try {
            Bundle bundle = toProcess.take();
            new MessengerSender(CONNECT_AND_RETURN_DATA).setData(bundle).send(messenger);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //ToDo: Tell server that we failed to process the task
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Connected");
        }
    };

    private final Emitter.Listener disconnectListenerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Disconnected");
        }
    };

    private final Emitter.Listener errorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Socket error occurred:");
            printArgs(args);
        }
    };

    private final Emitter.Listener connectErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Could not connect to server");
            printArgs(args);
        }
    };

    private final Emitter.Listener setIdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[1];
                id = message.getInt(ID);
                socket.emit(SOCKET_GET_CODE);
            } catch (JSONException e) {
                exitOnBadArgs(SOCKET_SET_ID, args);
            }
        }
    };

    private final Emitter.Listener setCodeListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Log.i(LOG_TAG, "Got code");
                JSONObject message = (JSONObject) args[0];
                String code = message.getString(CODE);
                String data = message.getString(DATA);

                String path = null;
                try {
                    path = writeToFile(code, "code.js", getApplicationContext());
                } catch (IOException e) {
                    Log.e(LOG_TAG ,"Could not create or write to code.js");
                    stopSelf();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(PATH, path);
                bundle.putString(DATA, data);

                toProcess.add(bundle);
            } catch (JSONException e) {
                exitOnBadArgs(SOCKET_SET_CODE, args);
            }
        }
    };

    private void exitOnBadArgs(String event, Object... args) {
        Log.e(LOG_TAG, "Malformed args for event:" + event);
        printArgs(args);
        stopSelf();
    }

    private void printArgs(Object... args) {
        for (Object arg : args) {
            Log.e(LOG_TAG, String.valueOf(arg));
        }
    }

    private String writeToFile(String data, String filename, Context context)
            throws IOException {
        File path = context.getFilesDir();
        File file = new File(path, filename);

        if (!file.exists() || !file.delete()) {
            throw new IOException("Failed to delete old file");
        }

        if (!file.createNewFile()) {
            throw new IOException("Failed to create file");
        }

        FileOutputStream stream = new FileOutputStream(file);

        try {
            stream.write(data.getBytes());
        } finally {
            stream.close();
        }

        return file.getPath();
    }
}
