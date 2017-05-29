package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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
import static com.phonemap.phonemap.constants.Sockets.CODE;
import static com.phonemap.phonemap.constants.Sockets.CONNECT_AND_RETURN_DATA;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.FAILED_EXECUTING_CODE;
import static com.phonemap.phonemap.constants.Sockets.GET_CODE;
import static com.phonemap.phonemap.constants.Sockets.ID;
import static com.phonemap.phonemap.constants.Sockets.PATH;
import static com.phonemap.phonemap.constants.Sockets.RETURN_DATA;
import static com.phonemap.phonemap.constants.Sockets.RETURN_RESULTS;
import static com.phonemap.phonemap.constants.Sockets.SET_CODE;
import static com.phonemap.phonemap.constants.Sockets.SET_ID;

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
                    Log.i(LOG_TAG, "Got connect message");
                    connectAndReturnData(msg.replyTo);
                    break;
                case RETURN_RESULTS:
                    Log.i(LOG_TAG, "Got return message");
                    //ToDo: Implement returning data to server
                    break;
                case FAILED_EXECUTING_CODE:
                    Log.i(LOG_TAG, "Got failed to execute message");
                    //ToDo: Implement returning errors to server
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

        socket.on(SET_ID, setIdListener);
        socket.on(SET_CODE, setCodeListener);

        socket.connect();

        try {
            returnCodeAndData(messenger);
        } catch (InterruptedException | RemoteException e) {
            Log.e(LOG_TAG, "Failed to return code and data");
            e.printStackTrace();
            stopSelf();
            //ToDo: Better error handling
        }
    }

    private void returnCodeAndData(Messenger messenger) throws InterruptedException, RemoteException {
        Bundle bundle = toProcess.take();

        Message msg = Message.obtain(null, RETURN_DATA);
        msg.setData(bundle);
        messenger.send(msg);
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
        }
    };

    private final Emitter.Listener setIdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[1];
                id = message.getInt(ID);
                socket.emit(GET_CODE);
            } catch (JSONException e) {
                exitOnBadArgs(SET_ID, args);
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
                }

                Bundle bundle = new Bundle();
                bundle.putString(PATH, path);
                bundle.putString(DATA, data);

                toProcess.add(bundle);
            } catch (JSONException e) {
                exitOnBadArgs(SET_CODE, args);
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
