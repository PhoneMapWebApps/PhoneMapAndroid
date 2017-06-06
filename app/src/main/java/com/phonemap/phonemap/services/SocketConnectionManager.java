package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.socket.client.IO;
import io.socket.client.Socket;

import static com.phonemap.phonemap.constants.Server.WS_URI;
import static com.phonemap.phonemap.constants.Sockets.CODE;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.EXECUTION_FAILED;
import static com.phonemap.phonemap.constants.Sockets.FAILED_EXECUTING_CODE;
import static com.phonemap.phonemap.constants.Sockets.GET_CODE;
import static com.phonemap.phonemap.constants.Sockets.ID;
import static com.phonemap.phonemap.constants.Sockets.PATH;
import static com.phonemap.phonemap.constants.Sockets.RETURN;
import static com.phonemap.phonemap.constants.Sockets.RETURN_DATA_AND_CODE;
import static com.phonemap.phonemap.constants.Sockets.RETURN_RESULTS;
import static com.phonemap.phonemap.constants.Sockets.SET_CODE;
import static com.phonemap.phonemap.constants.Sockets.START_CODE;
import static io.socket.emitter.Emitter.Listener;

public class SocketConnectionManager extends Service {
    private static String LOG_TAG = "SocketConnectionManager";
    final Listener disconnectListenerListener = new Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Disconnected");
        }
    };
    final Listener errorListener = new Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Socket error occurred:");
            printArgs(args);
        }
    };
    final Listener connectErrorListener = new Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Could not connect to server");
            printArgs(args);
        }
    };
    private final Messenger messenger = new Messenger(new MessageHandler());
    private final BlockingQueue<Messenger> waitingForWork = new LinkedBlockingQueue<>();
    private String id;
    private final JSONObject SIGNED_EMPTY_PAYLOAD = signWithID(new JSONObject());
    private Socket socket;
    final Listener connectListener = new Listener() {
        @Override
        public void call(Object... args) {
            id  = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            requestMoreWork();
        }
    };
    final Listener setCodeListener = new Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[args.length - 1];
                String code = message.getString(CODE);
                String data = message.getString(DATA);

                String path;

                try {
                    path = writeToFile(code, "code.js", getApplicationContext());
                } catch (IOException e) {
                    Log.e(LOG_TAG ,"Could not create or write to code.js\n(" + e.toString() + ")");
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(PATH, path);
                bundle.putString(DATA, data);

                if (!waitingForWork.isEmpty()) {
                    Messenger replyTo = waitingForWork.poll();
                    new MessengerSender(replyTo).setData(bundle).setMessage(RETURN_DATA_AND_CODE).send();
                    makeServerRequest(START_CODE);
                }
            } catch (JSONException e) {
                exitOnBadArgs(SET_CODE, args);
            }
        }
    };

    public SocketConnectionManager() {
        this(IO.socket(WS_URI));
    }

    public SocketConnectionManager(Socket socket) {
        setupSocketOnEvents(socket);
        socket.connect();
        this.socket = socket;
    }

    public void setupSocketOnEvents(Socket socket) {
        socket.on(Socket.EVENT_CONNECT, connectListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectListenerListener);
        socket.on(Socket.EVENT_ERROR, errorListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener);

        socket.on(SET_CODE, setCodeListener);
    }

    public void returnDataAndCode(Messenger replyTo) {
        if (!waitingForWork.contains(replyTo)) {
            waitingForWork.add(replyTo);
        } else {
            Log.e(LOG_TAG, "Should not be asking for work multiple times");
        }

        requestMoreWork();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void requestMoreWork() {
        if (socket.connected() && !waitingForWork.isEmpty()) {
            makeServerRequest(GET_CODE);
        }
    }

    private void exitOnBadArgs(String event, Object... args) {
        Log.e(LOG_TAG, "Malformed args for event:" + event);
        printArgs(args);
    }

    private void printArgs(Object... args) {
        int i = 0;
        for (Object arg : args) {
            Log.e(LOG_TAG, String.valueOf(i) + ": " + String.valueOf(arg));
            i++;
        }
    }

    private String writeToFile(String data, String filename, Context context)
            throws IOException {
        File path = context.getFilesDir();
        File file = new File(path, filename);

        if (file.exists() && !file.delete()) {
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

    private JSONObject signWithID(JSONObject object) {
        try {
            object.put(ID, id);
        } catch (JSONException e) {
            // Todo: Handle error  better
            Log.e(LOG_TAG, "Failed to sign JSON with id");
        }

        return object;
    }

    private void makeServerRequest(String apiEndPoint) {
        sendMessage(apiEndPoint, SIGNED_EMPTY_PAYLOAD);
    }

    private void sendToServer(String apiEndPoint, Message message) {
        JSONObject signedBundledPayload = signWithID(bundleToJSON(message.getData()));
        sendMessage(apiEndPoint, signedBundledPayload);
    }

    private void sendMessage(String apiEndPoint, JSONObject payload) {
        Log.i(LOG_TAG, "Tag: " + apiEndPoint + " Payload: " + String.valueOf(payload));
        socket.emit(apiEndPoint, payload);
    }

    public JSONObject bundleToJSON(Bundle bundle) {
        JSONObject json = new JSONObject();

        if (bundle == null || bundle.isEmpty()) {
            return json;
        }

        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                json.put(key, bundle.get(key));
            } catch(JSONException e) {
                Log.e(LOG_TAG, "Cannot create JSONObject out of provided bundle");
            }
        }

        return json;
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case RETURN_DATA_AND_CODE:
                    returnDataAndCode(message.replyTo);
                    break;
                case RETURN_RESULTS:
                    SocketConnectionManager.this.sendToServer(RETURN, message);
                    break;
                case FAILED_EXECUTING_CODE:
                    SocketConnectionManager.this.sendToServer(EXECUTION_FAILED, message);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }
}
