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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.phonemap.phonemap.constants.Phone;
import com.phonemap.phonemap.constants.Preferences;

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

import static com.phonemap.phonemap.constants.Requests.FORCE_TASK;
import static com.phonemap.phonemap.constants.Requests.TASK_ID;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Server.WS_URI;
import static com.phonemap.phonemap.constants.Sockets.CODE;
import static com.phonemap.phonemap.constants.Sockets.CODE_AVAILABLE;
import static com.phonemap.phonemap.constants.Sockets.COMPLETED_SUBTASK;
import static com.phonemap.phonemap.constants.Sockets.DATA;
import static com.phonemap.phonemap.constants.Sockets.EXECUTION_FAILED;
import static com.phonemap.phonemap.constants.Sockets.FAILED_EXECUTING_CODE;
import static com.phonemap.phonemap.constants.Sockets.ID;
import static com.phonemap.phonemap.constants.Sockets.NEW_SUBTASK;
import static com.phonemap.phonemap.constants.Sockets.NEW_TASK;
import static com.phonemap.phonemap.constants.Sockets.NO_TASKS;
import static com.phonemap.phonemap.constants.Sockets.PATH;
import static com.phonemap.phonemap.constants.Sockets.REQUEST_NEW_SUBTASK;
import static com.phonemap.phonemap.constants.Sockets.RESULT;
import static com.phonemap.phonemap.constants.Sockets.SET_CODE;
import static com.phonemap.phonemap.constants.Sockets.SUBTASK_STARTED;
import static io.socket.emitter.Emitter.Listener;

public class SocketConnectionManager extends Service {
    private static final String LOG_TAG = "SocketConnectionManager";

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
    final Listener noTaskListener = new Listener() {
        @Override
        public void call(Object... args) {
            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(NO_TASKS));
        }
    };
    private final BlockingQueue<Messenger> readyRunners = new LinkedBlockingQueue<>();
    final Listener codeAvailableListener = new Listener() {
        @Override
        public void call(Object... args) {
            new MessengerSender(getWaitingRunner()).setMessage(NEW_TASK).send();
        }
    };
    public Phone phone = new Phone(this);
    public Preferences preferences = new Preferences(this);
    private Socket socket;
    final Listener connectListener = new Listener() {
        @Override
        public void call(Object... args) {
            requestMoreWork();
        }
    };
    private final Messenger messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NEW_SUBTASK:
                    addReadyRunner(message.replyTo);
                    break;
                case COMPLETED_SUBTASK:
                    SocketConnectionManager.this.sendToServer(RESULT, message);
                    break;
                case FAILED_EXECUTING_CODE:
                    SocketConnectionManager.this.sendToServer(EXECUTION_FAILED, message);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    });
    final Listener setCodeListener = new Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[args.length - 1];
                String code = message.getString(CODE);
                String data = message.getString(DATA);
                String task_name = message.getString(TASK_NAME);

                String path;

                try {
                    path = writeToFile(code, "code.js", getApplicationContext());
                } catch (IOException e) {
                    Log.e(LOG_TAG ,"Could not create or write to code.js\n(" + e.toString() + ")");
                    return;
                }

                Bundle bundle = EMPTY_BUNDLE();
                bundle.putString(PATH, path);
                bundle.putString(DATA, data);
                bundle.putString(TASK_NAME, task_name);

                if (!readyRunners.isEmpty()) {
                    new MessengerSender(getWaitingRunner()).setMessage(NEW_SUBTASK).setData(bundle).send();
                    prodServer(SUBTASK_STARTED);
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
        this.socket = socket;
        this.socket.connect();
    }

    public Bundle EMPTY_BUNDLE() { return new Bundle(); }

    public void setupSocketOnEvents(Socket socket) {
        socket.on(Socket.EVENT_CONNECT, connectListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectListenerListener);
        socket.on(Socket.EVENT_ERROR, errorListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener);

        socket.on(NO_TASKS, noTaskListener);
        socket.on(SET_CODE, setCodeListener);
        socket.on(CODE_AVAILABLE, codeAvailableListener);
    }

    public void addReadyRunner(Messenger jsRunnerMessenger) {
        if (!readyRunners.contains(jsRunnerMessenger)) {
            readyRunners.add(jsRunnerMessenger);
        }

        requestMoreWork();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private  Messenger getWaitingRunner() {
        return readyRunners.poll();
    }

    private void requestMoreWork() {
        if (socket.connected() && !readyRunners.isEmpty()) {
            Bundle bundle = EMPTY_BUNDLE();
            bundle.putInt(TASK_ID, preferences.preferredTask());
            bundle.putBoolean(FORCE_TASK, preferences.autostartEnabled());

            sendToServer(REQUEST_NEW_SUBTASK, bundle);
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
            object.put(ID, phone.id());
        } catch (JSONException e) {
            // Todo: Handle error  better
            Log.e(LOG_TAG, "Failed to sign JSON with id");
        }

        return object;
    }

    public void prodServer(String apiEndPoint) {
        sendMessage(apiEndPoint, new JSONObject());
    }

    public void sendToServer(String apiEndPoint, Message message) {
        JSONObject bundledPayload = bundleToJSON(message.getData());
        sendMessage(apiEndPoint, bundledPayload);
    }

    public void sendToServer(String apiEndPoint, Bundle bundle) {
        JSONObject bundledPayload = bundleToJSON(bundle);
        sendMessage(apiEndPoint, bundledPayload);
    }

    private void sendMessage(String apiEndPoint, JSONObject payload) {
        JSONObject signedPayload = signWithID(payload);
        Log.i(LOG_TAG, "Tag: " + apiEndPoint + " Payload: " + String.valueOf(signedPayload));
        socket.emit(apiEndPoint, signedPayload);
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
}
